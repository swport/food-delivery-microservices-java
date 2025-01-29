package com.abc.inc.psa.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

public class DynamoDbClientConfig {

    public static DynamoDbEnhancedClient createDynamoDbEnhancedClient() {
        DynamoDbClient ddbClient;

        // when testing locally
        if (System.getenv("AWS_SAM_LOCAL") != null) {
            // Local environment configuration
            ddbClient = DynamoDbClient.builder()
                    .endpointOverride(URI.create(System.getenv("DYNAMO_LOCAL_URL")))
                    .region(Region.of(System.getenv("AWS_REGION")))
                    .credentialsProvider(getLocalCredentialsProvider())
                    .build();
        } else {
            // AWS Lambda environment configuration
            ddbClient = DynamoDbClient.create();
        }

        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddbClient)
                .build();
    }

    private static AwsCredentialsProvider getLocalCredentialsProvider() {
        // Use environment variables for local credentials
        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");

        if (accessKeyId == null || secretAccessKey == null) {
            throw new RuntimeException("AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY must be set for local development.");
        }

        return () -> AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }
}