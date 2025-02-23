package com.abc.inc.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abc.inc.cart.service.CartService;
import com.abc.inc.common.models.Cart;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CartService cartService = new CartService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {

            String httpMethod = input.getHttpMethod();
            String auth_user_id = (String)input.getRequestContext().getAuthorizer().get("user_id");
            String path = input.getPath();

            if(auth_user_id == null) {
                throw new RuntimeException("Invalid request2");
            }

            if("GET".equalsIgnoreCase(httpMethod)) {
                System.out.println("came here");
                List<Cart> all = getAll(auth_user_id);
                System.out.println("Data: " + all);
                String output = objectMapper.writeValueAsString(all);

                return response
                        .withStatusCode(200)
                        .withBody(output);
            }

            String requestBody = input.getBody();

            if("POST".equalsIgnoreCase(httpMethod) && (requestBody == null || requestBody.isBlank())) {
                throw new RuntimeException("Invalid request1");
            }

            final Map<String, Object> postBody = objectMapper.readValue(requestBody, new TypeReference<>() {});

            System.out.println("came here2");

            String errors = null;

            if(path != null) {
                errors = switch (path) {
                    case "/cart/add" -> add(auth_user_id, postBody);
                    case "/cart/update" -> update(auth_user_id, postBody);
                    case "/cart/remove" -> remove(auth_user_id, postBody);
                    default -> throw new IllegalStateException("Unexpected value: " + path);
                };
            }

            if(errors != null && !errors.isBlank()) {
                return response
                        .withBody(errors)
                        .withStatusCode(400);
            }

            return response
                    .withStatusCode(200);

        } catch (Exception e) {
            e.printStackTrace();
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private List<Cart> getAll(String user_id) {
        return cartService.getCartItemsByUserId(user_id);
    }

    private String remove(String userId, Map<String, Object> postBody) {
        try {
            String productId = (String)postBody.get("product_id");
            cartService.deleteCartItem(userId, productId);
            return "";
        } catch(Exception e) {
            return "Internal server error";
        }
    }

    private String add(String userId, Map<String, Object> postBody) {
        try {
            String productId = (String)postBody.get("product_id");
            String productName = (String)postBody.get("product_name");
            String qty = (String)postBody.get("qty");

            if(productId == null || productName == null || qty == null) {
                return "Invalid input";
            }

            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setProductName(productName);
            cart.setProductQty(qty);

            cartService.addItemToCart(cart);
            return "";
        } catch(Exception e) {
            return "Internal server error";
        }
    }

    private String update(String userId, Map<String, Object> postBody) {
        try {
            String productId = (String)postBody.get("product_id");
            String qty = (String)postBody.get("qty");
            cartService.updateCartItem(userId, productId, qty);
            return "";
        } catch(Exception e) {
            return "Internal server error";
        }

    }
}
