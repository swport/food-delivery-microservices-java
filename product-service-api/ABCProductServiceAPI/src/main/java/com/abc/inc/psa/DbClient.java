package com.abc.inc.psa;

import com.abc.inc.common.configs.DynamoDbClientConfig;
import com.abc.inc.common.models.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;

public class DbClient {
    private static final String TABLE_NAME = "ABC_Products";
    private static final String GSI_INDEX_NAME = "ABC_Restaurants";

    private final static DynamoDbEnhancedClient dynamoDbEnhancedClient
            = DynamoDbClientConfig.createDynamoDbEnhancedClient();
    private final static TableSchema<Product> PRODUCT_TABLE_SCHEMA
            = TableSchema.fromBean(Product.class);

    private static final Logger log = LoggerFactory.getLogger(DbClient.class);

    public static List<Product> getProductsByRestaurantId(String rest_id) {
        List<Product> result = new ArrayList<>();
        final DynamoDbTable<Product> mappedTable = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(rest_id).build()
        );

        QueryEnhancedRequest queryRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .build();

        PageIterable<Product> results = mappedTable.query(queryRequest);

        for (Page<Product> page : results) {
            result.addAll(page.items());
        }
        return result;
    }

    public static List<Product> getRestaurants() {
        List<Product> result = new ArrayList<>();

        DynamoDbTable<Product> table = dynamoDbEnhancedClient.table(TABLE_NAME, PRODUCT_TABLE_SCHEMA);
        DynamoDbIndex<Product> gsi = table.index(GSI_INDEX_NAME);

        SdkIterable<Page<Product>> results = gsi.scan(ScanEnhancedRequest.builder().build());

        for (Page<Product> page : results) {
            result.addAll(page.items());
        }
        return result;
    }
}
