package com.abc.inc.orderplacement.service;

import com.abc.inc.common.configs.DynamoDbClientConfig;
import com.abc.inc.common.models.Order;
import com.abc.inc.common.models.Cart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.abc.inc.common.Constants.ORDER_DDB_TABLE;
import static com.abc.inc.common.Constants.CART_DDB_TABLE;

public class OrderPlacementService {
    private final static DynamoDbEnhancedClient dynamoDbEnhancedClient
            = DynamoDbClientConfig.createDynamoDbEnhancedClient();
    private final static TableSchema<Cart> CART_TABLE_SCHEMA
            = TableSchema.fromBean(Cart.class);
    private final static TableSchema<Order> ORDER_TABLE_SCHEMA
            = TableSchema.fromBean(Order.class);

    private final DynamoDbTable<Order> orderTable;
    private final DynamoDbTable<Cart> cartTable;

    public OrderPlacementService() {
        orderTable = dynamoDbEnhancedClient.table(ORDER_DDB_TABLE, ORDER_TABLE_SCHEMA);
        cartTable = dynamoDbEnhancedClient.table(CART_DDB_TABLE, CART_TABLE_SCHEMA);
    }

    public String createNewOrder(String userId, String delivery_address) {
        String orderStr = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Order order =null;
            // get existing order
            Optional<Order> existingOrder = getOrders(userId).stream()
                    .filter(o -> "pending".equals(o.getOrderStatus()))
                    .findFirst();

            if(existingOrder.isPresent()) {
                order = existingOrder.get();
            } else {
                String orderUUID = UUID.randomUUID().toString();
                List<Cart> cartItems = getCartItemsByUserId(userId);
                String cartItemsJsonStr = objectMapper.writeValueAsString(cartItems);

                order = new Order();
                order.setUserId(userId);
                order.setOrderId(orderUUID);
                order.setDeliveryAddress(delivery_address);
                order.setOrderStatus("pending");
                order.setOrderTime(String.valueOf(Instant.now().getEpochSecond()));
                order.setOrderDetails(cartItemsJsonStr);

                orderTable.putItem(order);
            }

            orderStr = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            System.out.println("Json processing error: "+e);
        } catch(Exception e) {
            System.out.println("Error: "+e);
        }

        return orderStr;
    }

    public List<Order> getOrders(String userId) {
        List<Order> result = new ArrayList<>();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build()
        );
        QueryEnhancedRequest queryRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .build();
        PageIterable<Order> results = orderTable.query(queryRequest);
        for (Page<Order> page : results) {
            result.addAll(page.items());
        }
        return result;
    }

    public List<Cart> getCartItemsByUserId(String userId) {
        List<Cart> result = new ArrayList<>();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build()
        );
        QueryEnhancedRequest queryRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .build();
        PageIterable<Cart> results = cartTable.query(queryRequest);
        for (Page<Cart> page : results) {
            result.addAll(page.items());
        }
        return result;
    }
}
