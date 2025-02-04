package com.abc.inc.pca.dto;

import java.math.BigDecimal;
import java.util.Optional;

public record ProductRequest(
        String dish_name,
        BigDecimal price,
        Optional<String> restaurant_name,
        Optional<String> restaurant_location
) {
    public ProductRequest(String dish_name, BigDecimal price) {
        this(dish_name, price, Optional.empty(), Optional.empty());
    }

    public static ProductRequest of(String dish_name, BigDecimal price, String restaurant_name, String restaurant_location) {
        return new ProductRequest(
                dish_name,
                price,
                (restaurant_name != null) ? Optional.of(restaurant_name) : Optional.empty(),
                (restaurant_location != null) ? Optional.of(restaurant_location) : Optional.empty()
        );
    }
}