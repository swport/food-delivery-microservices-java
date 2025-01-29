package com.abc.inc.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Authorizer implements RequestStreamHandler {

    private static final String HMA_SECRET = "iKFZCbuJViWW4YFx9ZRfE4qNfNBAM65NZ+Eauqr+h/w=";
    private static final String ISSUER_KEY = "AbcIncAuth";

    private final static Logger LOGGER = LoggerFactory.getLogger(Authorizer.class);
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public void handleRequest(InputStream input, OutputStream output, Context context) {
        // Using a RequestSteamHandler here because there doesn't seem to be a way to get
        // a hold of the internal Jackson ObjectMapper from AWS to adjust it to deal with
        // the uppercase property names in the Policy document
        TokenAuthorizerRequest event = fromJson(input, TokenAuthorizerRequest.class);
        if (null == event) {
            throw new RuntimeException("Can't deserialize input");
        }

        AuthorizerResponse response;
        DecodedJWT token = verifyToken(event);
        if (token == null) {
            LOGGER.error("JWT not verified. Returning Not Authorized");
            response = AuthorizerResponse.builder()
                    .principalId(event.getAccountId())
                    .policyDocument(PolicyDocument.builder()
                            .statement(Statement.builder()
                                    .effect("Deny")
                                    .resource(apiGatewayResource(event))
                                    .build()
                            )
                            .build()
                    )
                    .context(new HashMap<>())
                    .build();
        } else {
            LOGGER.info("JWT verified. Returning Authorized.");
            String tenantId = getTenantId(token);

            // Pass the tenant id back to API Gateway so we can map it to a custom
            // HTTP header value -- will be available as context.authorizer.TenantId
            // in the Integration Request configuration of the API method
            Map<String, String> extraContext = new HashMap<>();
            extraContext.put("TenantId", "HelloWorld");

            // This authorizer is shared across our API, so we are just going to
            // grant access to all REST Resources of all HTTP methods defined for
            // this API for this Stage in this Region for this Account
            response = AuthorizerResponse.builder()
                    .principalId(event.getAccountId())
                    .policyDocument(PolicyDocument.builder()
                            .statement(Statement.builder()
                                    .effect("Allow")
                                    .resource(apiGatewayResource(event))
                                    .build()
                            )
                            .build()
                    )
                    .context(extraContext)
                    .build();
        }

        try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            writer.write(toJson(response));
            writer.flush();
        } catch (Exception e) {
            LOGGER.error(getFullStackTrace(e));
            throw new RuntimeException(e.getMessage());
        }
    }

    protected DecodedJWT verifyToken(TokenAuthorizerRequest request) {
        String userPoolId = getTokenIssuer(request.tokenPayload());
        Algorithm algorithm = Algorithm.HMAC256(HMA_SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER_KEY)
                .build();
        DecodedJWT token = null;
        try {
            token = verifier.verify(request.tokenPayload());
        } catch (JWTVerificationException e) {
            LOGGER.error(getFullStackTrace(e));
        }
        return token;
    }

    protected String getTokenIssuer(String token) {
        String issuer = JWT.decode(token).getClaim("iss").asString();
        return issuer.substring(issuer.lastIndexOf("/") + 1);
    }

    protected String getTenantId(DecodedJWT token) {
        return token.getClaim("custom:tenant_id").asString();
    }

    protected String apiGatewayResource(TokenAuthorizerRequest event) {
        return apiGatewayResource(event, "*", "*");
    }

    protected String apiGatewayResource(TokenAuthorizerRequest event, String method, String resource) {
        String arn = String.format("arn:%s:execute-api:%s:%s:%s/%s/%s/%s",
                Region.of(event.getRegion()).metadata().partition().id(),
                event.getRegion(),
                event.getAccountId(),
                event.getApiId(),
                event.getStage(),
                method,
                resource
        );
        return arn;
    }
    protected String toJson(Object obj) {
        String json = null;
        try {
            json = MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.error(getFullStackTrace(e));
        }
        return json;
    }

    protected <T> T fromJson(InputStream json, Class<T> serializeTo) {
        T object = null;
        try {
            object = MAPPER.readValue(json, serializeTo);
        } catch (Exception e) {
            LOGGER.error(getFullStackTrace(e));
        }
        return object;
    }

    protected String getFullStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

}