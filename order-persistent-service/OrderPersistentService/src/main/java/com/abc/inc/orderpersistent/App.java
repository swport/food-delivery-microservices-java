package com.abc.inc.orderpersistent;

import com.abc.inc.common.Utils;
import com.abc.inc.common.configs.RdbClient;
import com.abc.inc.common.models.Cart;
import com.abc.inc.common.models.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class App implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Connection rdbClientConnection;
    private PreparedStatement preparedStatement;

    @Override
    public Void handleRequest(SQSEvent event, final Context context) {
        createRecords(event, context);
        return null;
    }

    private void createRecords(SQSEvent event, final Context context) {
        Connection rdbClientConnection = null;
        try {
            RdbClient rdbClient = new RdbClient();
            rdbClientConnection = rdbClient.getConnection();

            rdbClientConnection.setAutoCommit(false);

            for (SQSEvent.SQSMessage message : event.getRecords()) {
                createDbRecord(message.getBody());
            }

            rdbClientConnection.commit();
        } catch (Exception e) {
            System.out.println("Error: "+e);
        } finally {
            Utils.closeRdbConnection(rdbClientConnection, preparedStatement);
        }
    }

    private void createDbRecord(String orderStr) {
        try {
            Order order = objectMapper.readValue(orderStr, Order.class);
            List<Cart> cartItems = objectMapper.readValue(order.getOrderDetails(), new TypeReference<>() {});

            int user_id = Integer.parseInt(order.getUserId());

            String query = "INSERT INTO orders (user_id, order_id, order_status, delivery_address, order_time)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?);";

            preparedStatement = rdbClientConnection.prepareStatement(query);

            preparedStatement.setInt(1, user_id);
            preparedStatement.setString(2, order.getOrderId());
            preparedStatement.setString(3, order.getOrderStatus());
            preparedStatement.setString(4, order.getDeliveryAddress());
            preparedStatement.setString(4, order.getOrderTime());
            preparedStatement.execute();

            String queryCart = "INSERT INTO order_items (order_id, item_name, item_qty) VALUES (?, ?, ?)";

            preparedStatement = rdbClientConnection.prepareStatement(queryCart);

            for(Cart cartItem: cartItems) {
                preparedStatement.setString(1, order.getOrderId());
                preparedStatement.setString(2, cartItem.getProductName());
                preparedStatement.setInt(3, Integer.parseInt(cartItem.getProductQty()));

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

        } catch(JsonProcessingException jsonProcessingException) {
            System.out.println("Json processing error: "+jsonProcessingException);
        } catch(SQLException e) {
            System.out.println("SQL exception:  "+e);
        }
    }
}
