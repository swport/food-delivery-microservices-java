package com.abc.inc.cart.service;

import com.abc.inc.common.configs.DynamoDbClientConfig;
import com.abc.inc.common.models.Cart;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static final String TABLE_NAME = "ABC_Cart";

    private final static DynamoDbEnhancedClient dynamoDbEnhancedClient
            = DynamoDbClientConfig.createDynamoDbEnhancedClient();
    private final static TableSchema<Cart> CART_TABLE_SCHEMA
            = TableSchema.fromBean(Cart.class);

    private final DynamoDbTable<Cart> cartTable;

    public CartService() {
        cartTable = dynamoDbEnhancedClient.table(TABLE_NAME, CART_TABLE_SCHEMA);
    }

    public Cart addItemToCart(Cart cart) {
        cartTable.putItem(cart);
        return cart;
    }

    public Cart getCartItem(String userId, String productId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(productId)
                .build();
        return cartTable.getItem(r -> r.key(key));
    }

    public Cart updateCartItem(String userId, String productId, String qty) {
        Cart cart = getCartItem(userId, productId);
        if(cart != null && qty != null) {
            if(qty.trim().equals("0")) {
                cartTable.deleteItem(cart);
            } else {
                cart.setProductQty(qty);
                cartTable.updateItem(cart);
            }
        }
        return cart;
    }

    public void deleteCartItem(String userId, String productId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(productId)
                .build();
        cartTable.deleteItem(r -> r.key(key));
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
