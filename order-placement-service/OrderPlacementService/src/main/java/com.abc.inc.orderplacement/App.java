package com.abc.inc.orderplacement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abc.inc.common.configs.SqsMessageSender;
import com.abc.inc.common.models.Order;
import com.abc.inc.orderplacement.service.OrderPlacementService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final OrderPlacementService orderPlacementService = new OrderPlacementService();
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

            if(auth_user_id == null) {
                throw new RuntimeException("Invalid request2");
            }

            if("GET".equalsIgnoreCase(httpMethod)) {
                List<Order> all = orderPlacementService.getOrders(auth_user_id);
                String output = objectMapper.writeValueAsString(all);

                return response
                        .withStatusCode(200)
                        .withBody(output);
            }

            String requestBody = input.getBody();

            if(!"POST".equalsIgnoreCase(httpMethod) || requestBody == null || requestBody.isBlank()) {
                throw new RuntimeException("Invalid request1");
            }

            final Map<String, Object> postBody = objectMapper.readValue(requestBody, new TypeReference<>() {});

            if(postBody.get("delivery_address") == null) {
                throw new RuntimeException("Invalid request1");
            }

            String orderJSON = orderPlacementService.createNewOrder(
                    auth_user_id, String.valueOf(postBody.get("delivery_address"))
            );

            System.out.println("sending sqs event new order: "+System.getenv("SQS_QUEUE_URL"));

            if(orderJSON != null) {
                SqsMessageSender.sendMessage(orderJSON);
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
}
