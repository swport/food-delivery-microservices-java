package com.abc.inc.cart.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Cart {
    private String user_id;
    private String product_id;
    private String product_name;
    private String product_qty;

    public Cart() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getUserId() {
        return user_id;
    }

    public void setUserId(String uid) {
        user_id = uid;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getProductId() {
        return product_id;
    }

    public void setProductId(String pid) {
        product_id = pid;
    }

    public String getProductName() {
        return product_name;
    }

    public void setProductName(String pname) {
        product_name = pname;
    }

    public String getProductQty() {
        return product_qty;
    }

    public void setProductQty(String pqty) {
        product_qty = pqty;
    }
}