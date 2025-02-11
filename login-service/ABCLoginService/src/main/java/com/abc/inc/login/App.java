package com.abc.inc.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Map<String, String> tempUserInfo = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static APIGatewayProxyResponseEvent response;
    private static ObjectMapper objectMapper;

    static {
        try {
            final Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            response = new APIGatewayProxyResponseEvent()
                    .withHeaders(headers);

            objectMapper = new ObjectMapper();
        } catch(Exception e) {
            log.error("Static initialization failure: ", e);
        }
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        final Map<String, String> queryParams = input.getQueryStringParameters();

        String output = "";
        final String requestBody = input.getBody();

        try {

            if(!"POST".equals(input.getHttpMethod()) || requestBody == null || requestBody.isBlank()) {
                throw new RuntimeException("Invalid request");
            }

            final Map<String, Object> postBody = objectMapper.readValue(requestBody, new TypeReference<>() {});

            final String email = (String)postBody.get("email");
            final String password = (String)postBody.get("password");

            if(email == null || password == null) {
                throw new RuntimeException("Invalid input");
            }

            if(checkCredentialsAndSetUser(email, password)) {
                output = generateToken();
            } else {
                return response
                        .withStatusCode(400)
                        .withBody("Invalid credentials");
            }

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (JsonProcessingException e) {
            output = "Internal server error1";
            System.out.println(e);
            log.error("Json processing error: ", e);
        } catch (RuntimeException e) {
            System.out.println(e);
            output = "Internal server error2";
            log.error("Runtime exception: ", e);
        }

        return response
                .withBody(output)
                .withStatusCode(500);
    }

    private static boolean checkCredentialsAndSetUser(String email, String password) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            final String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            System.out.println("passwordHash: "+passwordHash);

            RdbClient mysqlClient = new RdbClient();
            connection = mysqlClient.getConnection();

            String query = "SELECT u.id,u.full_name,u.email,u.role,u.password," +
                    "GROUP_CONCAT(r.id) AS restaurant_ids " +
                    "FROM users u " +
                    "LEFT JOIN restaurants r ON u.id = r.user_id " +
                    "WHERE u.email = ? " +
                    "GROUP BY u.id";
            statement = connection.prepareStatement(query);
            statement.setString(1, email);
            resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    tempUserInfo.put("user_id", resultSet.getString(1));
                    tempUserInfo.put("user_name", resultSet.getString(2));
                    tempUserInfo.put("user_email", resultSet.getString(3));
                    tempUserInfo.put("user_role", resultSet.getString(4));
                    tempUserInfo.put("restaurant_ids", resultSet.getString(6));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: "+ e);
            log.error("Database error: ", e);
        } finally {
            if(resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error closing resultSet: ", e);
                }
            }
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.error("Error closing statement: ", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection: ", e);
                }
            }
        }
        return false;
    }

    private static String generateToken() {
        Algorithm algorithm = Algorithm.HMAC256(Constants.HMA_SECRET);
        JWTCreator.Builder builder = JWT.create()
                .withIssuer(Constants.ISSUER_KEY)
                .withSubject(Constants.ISSUER_KEY);

        for (Map.Entry<String, String> entry : tempUserInfo.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue());
        }

        return builder.sign(algorithm);
    }
}
