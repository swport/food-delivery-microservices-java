package com.abc.inc.common.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Order {
    private String user_id;
    private String order_id;
    private String order_status;
    private String delivery_address;
    private String order_time;
    private String order_details;

    public Order() {}

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
    public String getOrderId() {
        return order_id;
    }

    public String getDeliveryAddress() {
        return delivery_address;
    }

    public void setDeliveryAddress(String delivery_address) {
        this.delivery_address = delivery_address;
    }

    public void setOrderId(String oid) {
        order_id = oid;
    }

    public void setOrderStatus(String ostatus) {
        order_status = ostatus;
    }

    public String getOrderStatus() {
        return order_status;
    }

    public void setOrderTime(String otime) {
        order_time = otime;
    }

    public String getOrderTime() {
        return order_time;
    }

    public void setOrderDetails(String odetails) {
        order_details = odetails;
    }

    public String getOrderDetails() {
        return order_details;
    }
}