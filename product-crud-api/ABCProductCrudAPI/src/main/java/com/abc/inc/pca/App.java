package com.abc.inc.pca;

import java.math.BigDecimal;
import java.util.*;

import com.abc.inc.pca.dto.ProductRequest;
import com.abc.inc.pca.dto.RestaurantProfile;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        final String path = input.getPath();

        try {

            if(!"POST".equals(input.getHttpMethod()) || requestBody == null || requestBody.isBlank()) {
                throw new RuntimeException("Invalid request");
            }

            String user_role = (String)input.getRequestContext().getAuthorizer().get("user_role");
            String user_id = (String)input.getRequestContext().getAuthorizer().get("user_id");
            if(user_id == null || user_role == null || !user_role.trim().equals("owner")) {
                throw new RuntimeException("Invalid access");
            }

            final Map<String, Object> postBody = objectMapper.readValue(requestBody, new TypeReference<>() {});

            final String restaurant_ids = (String)input.getRequestContext().getAuthorizer().get("restaurant_ids");
            if(restaurant_ids == null || postBody.get("restaurant_id") == null) {
                throw new RuntimeException("Invalid request1");
            }

            String queryRestId = (String)postBody.get("restaurant_id");

            List<String> user_restaurant_ids = Arrays.asList(restaurant_ids.split(","));
            if(!user_restaurant_ids.contains(queryRestId)) {
                throw new RuntimeException("Invalid request2");
            }

            String errors = null;
            if(path != null) {
                switch (path) {
                    case "/restaurants/crud/create-product":
                        errors = addProduct(queryRestId, postBody);
                        break;
                    case "/restaurants/crud/update-product":
                        errors = updateProduct(queryRestId, postBody);
                        break;
                    case "/restaurants/crud/remove-product":
                        errors = removeProduct(queryRestId, postBody);
                        break;
                    case "/restaurants/crud/restaurant-update":
                        errors = updateRestaurant(queryRestId, user_id, postBody);
                        break;
                }
            }

            if(errors != null && !errors.isBlank()) {
                return response
                        .withBody(errors)
                        .withStatusCode(400);
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

    public String addProduct(String rest_id, Map<String, Object> postBody) {
        String errors = "";

        String dish_name = (String)postBody.get("dish_name");
        String price = (String)postBody.get("price");
        String restaurant_name = (String)postBody.get("restaurant_name");
        String restaurant_location = (String)postBody.get("restaurant_location");

        if(dish_name == null
                || price == null
                || restaurant_name == null
                || restaurant_location == null) {
            return "Invalid input. Please check all fields and try again.";
        }

        try {
            ProductRequest product = ProductRequest.of(
                    dish_name, new BigDecimal(price),
                    restaurant_name, restaurant_location
            );

            ProductRepository.addNewProduct(rest_id, product);
        } catch (Exception e) {
            errors = "Internal server error";
            log.error("Product add controller failure: ", e);
        }

        return errors;
    }

    public String updateProduct(String rest_id, Map<String, Object> postBody) {
        String errors = "";

        String product_id = (String)postBody.get("product_id");
        String dish_name = (String)postBody.get("dish_name");
        String price = (String)postBody.get("price");

        if(dish_name == null || price == null) {
            return "Invalid input. Please check all fields and try again.";
        }

        try {
            ProductRequest product = new ProductRequest(dish_name, new BigDecimal(price));
            ProductRepository.updateProductDetail(rest_id, product_id, product);
        } catch (Exception e) {
            errors = "Internal server error";
            log.error("Product update controller failure: ", e);
        }

        return errors;
    }

    public String removeProduct(String rest_id, Map<String, Object> postBody) {
        String errors = "";
        String product_id = (String)postBody.get("product_id");
        if(product_id == null) {
            return "Invalid input. Please check all fields and try again.";
        }

        try {
            ProductRepository.deleteProduct(rest_id, product_id);
        } catch (Exception e) {
            errors = "Internal server error";
            log.error("Product update controller failure: ", e);
        }

        return errors;
    }

    public String updateRestaurant(String rest_id, String user_id, Map<String, Object> postBody) {
        String errors = "";
        String restaurant_name = (String)postBody.get("restaurant_name");
        String restaurant_location = (String)postBody.get("restaurant_location");

        if(restaurant_name == null || restaurant_location == null) {
            return "Invalid input. Please check all fields and try again.";
        }

        try {
            RestaurantProfile restaurantProfile = new RestaurantProfile(
                    Integer.valueOf(rest_id),
                    restaurant_name, restaurant_location
            );
            boolean ok = RestaurantRepository.updateRestaurantProfile(restaurantProfile, Integer.valueOf(user_id));
            if(!ok) errors = "Error occurred while saving info.";
        } catch (Exception e) {
            errors = "Internal server error";
            log.error("Product update controller failure: ", e);
        }

        return errors;
    }
}
