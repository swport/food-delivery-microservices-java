package com.abc.inc.pca;

import com.abc.inc.common.configs.DynamoDbClientConfig;
import com.abc.inc.common.models.Product;
import com.abc.inc.pca.dto.ProductRequest;
import com.abc.inc.pca.dto.RestaurantProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductService {
    private static final String TABLE_NAME = "ABC_Products";

    private final static DynamoDbEnhancedClient dynamoDbEnhancedClient
            = DynamoDbClientConfig.createDynamoDbEnhancedClient();
    private final static TableSchema<Product> PRODUCT_TABLE_SCHEMA
            = TableSchema.fromBean(Product.class);

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public static boolean addNewProduct(String rest_id, ProductRequest productRequest) {
        final DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);
        boolean ok = true;
        try {
            final String product_id = UUID.randomUUID().toString();

            getProductsByRestaurantId(rest_id).stream()
                    .filter(p -> (p.getLatest() != null && p.getLatest().equals("1"))) //Find the current latest product
                    .findFirst()
                    .ifPresent(p -> {
                        p.setLatest(null);
                        productTable.updateItem(p);
                    });

            Product product = new Product();
            product.setLatest("1");
            product.setProductId(product_id);
            product.setRestaurantId(rest_id);
            product.setRestaurant_name(productRequest.restaurant_name().orElse(""));
            product.setRestaurant_location(productRequest.restaurant_location().orElse(""));
            product.setPrice(productRequest.price());
            product.setDish_name(productRequest.dish_name());

            productTable.putItem(product);
        } catch (DynamoDbException e) {
            log.info("Error: ", e);
            ok = false;
        }

        return ok;
    }

    public static boolean deleteProduct(String rest_id, String product_id) {
        final DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);
        boolean ok = true;

        try {
            Key key = Key.builder()
                    .partitionValue(rest_id)
                    .sortValue(product_id)
                    .build();

            Product existingProduct = productTable.getItem(key);
            if(existingProduct == null) {
                return true;
            }

            boolean latest = existingProduct.getLatest() != null && existingProduct.getLatest().equals("1");

            productTable.deleteItem(key);

            if(latest) {
                getProductsByRestaurantId(rest_id).stream()
                        .filter(p -> !p.getProductId().equals(product_id)) //Find the current latest product
                        .findFirst()
                        .ifPresent(p -> {
                            p.setLatest("1");
                            productTable.updateItem(p);
                        });
            }
        } catch(DynamoDbException e) {
            log.info("Error: ", e);
            ok = false;
        }

        return ok;
    }

    public static boolean updateRestaurantInfo(RestaurantProfile restaurantProfile) {
        final DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);
        boolean ok = true;

        try {
            getProductsByRestaurantId(restaurantProfile.id().toString())
                    .forEach(p -> {
                        p.setRestaurant_name(restaurantProfile.name());
                        p.setRestaurant_location(restaurantProfile.location());
                        productTable.updateItem(p);
                    });
        } catch (DynamoDbException e) {
            log.info("Error: ", e);
            ok = false;
        }
        return ok;
    }

    public static boolean updateProductDetail(String rest_id, String product_id, ProductRequest productRequest) {
        final DynamoDbTable<Product> productTable = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);
        boolean ok = true;

        try {
            Key key = Key.builder()
                    .partitionValue(rest_id)
                    .sortValue(product_id)
                    .build();

            Product existingProduct = productTable.getItem(key);
            if(existingProduct == null) {
                return false;
            }

            existingProduct.setDish_name(productRequest.dish_name());
            existingProduct.setPrice(productRequest.price());

            // Prepare the update request
            UpdateItemEnhancedRequest<Product> updateRequest = UpdateItemEnhancedRequest.builder(Product.class)
                    .item(existingProduct)
                    .build();

            productTable.updateItem(updateRequest);

        } catch (DynamoDbException e) {
            log.info("Error: ", e);
            ok = false;
        }
        return ok;
    }

    public static List<Product> getProductsByRestaurantId(String rest_id) {
        return getProductsByRestaurantId(rest_id, null);
    }

    public static List<Product> getProductsByRestaurantId(String rest_id, Integer limit) {
        List<Product> result = new ArrayList<>();
        final DynamoDbTable<Product> mappedTable = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(rest_id).build()
        );

        QueryEnhancedRequest.Builder queryBuilder = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional);

        if(limit != null) {
            queryBuilder.limit(limit);
        }

        QueryEnhancedRequest queryRequest = queryBuilder.build();

        PageIterable<Product> results = mappedTable.query(queryRequest);

        for (Page<Product> page : results) {
            result.addAll(page.items());
        }
        return result;
    }
}
