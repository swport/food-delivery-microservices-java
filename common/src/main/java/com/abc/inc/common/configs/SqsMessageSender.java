package com.abc.inc.common.configs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.net.URI;

public class SqsMessageSender {
    public static boolean sendMessage(String messageBody) {
        try {
            String queueUrl = System.getenv("SQS_QUEUE_URL");

            SqsClient sqsClient = SqsClient.create();

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);

            sqsClient.close();

            return true;
        } catch (SqsException e) {
            System.err.println("Error sending message to SQS: " + e);
        }

        return false;
    }
}
