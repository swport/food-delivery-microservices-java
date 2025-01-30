package com.abc.inc.psa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abc.inc.psa.dto.ProductResponse;
import com.abc.inc.psa.dto.RestaurantResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, String> queryParams = input.getQueryStringParameters();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            String output;

            final String RESTAURANT_ID_QUERY_PARAM = "rest_id";
            if(queryParams != null && queryParams.get(RESTAURANT_ID_QUERY_PARAM) != null) {
                output = objectMapper.writeValueAsString(getAllProductsByRestaurantId(queryParams.get(RESTAURANT_ID_QUERY_PARAM)));
            } else {
                output = objectMapper.writeValueAsString(getAllRestaurants());
            }

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Exception e) {
            log.error("Error: ", e);
            System.out.println("Error: "+Util.getFullStackTrace(e));
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    public static List<RestaurantResponse> getAllRestaurants() {
        return DbClient.getRestaurants().stream()
                .map(dto -> new RestaurantResponse(
                        dto.getRestaurantId(), dto.getRestaurant_name(), dto.getRestaurant_location()
                ))
                .toList();
    }

    public static List<ProductResponse> getAllProductsByRestaurantId(String rest_id) throws Exception {
        if(!rest_id.matches("[0-9]+"))
            throw new Exception("Invalid restaurant id");

        return DbClient.getProductsByRestaurantId(rest_id).stream()
                .map(dto -> new ProductResponse(
                        dto.getProductId(), dto.getDish_name(), dto.getRestaurantId()
                ))
                .toList();
    }

}