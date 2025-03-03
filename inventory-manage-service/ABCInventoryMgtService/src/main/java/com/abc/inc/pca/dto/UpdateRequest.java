package com.abc.inc.pca.dto;

public class UpdateRequest {
    private String restaurantId;
    private String product;
    private String newDishName;
    private Double newPrice;

    // Getters and setters
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getNewDishName() { return newDishName; }
    public void setNewDishName(String newDishName) { this.newDishName = newDishName; }

    public Double getNewPrice() { return newPrice; }
    public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }
}
