package com.abc.inc.pca.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;

@DynamoDbBean
public class Product {
    private static final String RESTAURANT_INDEX = "ABC_Restaurants";

    private String product_id;
    private String restaurant_id;
    private String restaurant_name;
    private String restaurant_location;
    private String dish_name;
    private String description;
    private String images;
    private BigDecimal price;
    private String latest;

    public Product() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    @DynamoDbSecondarySortKey(indexNames = {RESTAURANT_INDEX})
    public String getRestaurantId() {
        return restaurant_id;
    }

    public void setRestaurantId(String rest_id) {
        restaurant_id = rest_id;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getProductId() {
        return product_id;
    }

    public void setLatest(String str) {
        latest = str;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {RESTAURANT_INDEX})
    public String getLatest() {
        return latest;
    }

    public void setProductId(String p_id) {
        product_id = p_id;
    }

    public String getRestaurant_name() {
        return restaurant_name;
    }

    public void setRestaurant_name(String restaurant_name) {
        this.restaurant_name = restaurant_name;
    }

    public String getRestaurant_location() {
        return restaurant_location;
    }

    public void setRestaurant_location(String restaurant_location) {
        this.restaurant_location = restaurant_location;
    }

    public String getDish_name() {
        return dish_name;
    }

    public void setDish_name(String dish_name) {
        this.dish_name = dish_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

}