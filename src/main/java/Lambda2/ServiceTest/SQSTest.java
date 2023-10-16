package Lambda2.ServiceTest;

import Lambda2.SqsMessageBodyEntity.CuisineEmailPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.HashMap;
import java.util.Map;

public class SQSTest {

    public static void receiveSQSMessages(String queueUrl, SqsClient sqsClient) {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().
                queueUrl(queueUrl).
                maxNumberOfMessages(1).
                build();

        Map<String, String> cuisineEmailMap = new HashMap<>();

        for (Message message : sqsClient.receiveMessage(receiveMessageRequest).messages()) {
            System.out.println(message.messageId());
            System.out.println(message.body());

            ObjectMapper mapper = new ObjectMapper();
            CuisineEmailPair cuisineEmailPair = new CuisineEmailPair();

            try{
                cuisineEmailPair = mapper.readValue(message.body(), CuisineEmailPair.class);
            }catch (Exception e){
                e.printStackTrace();
            }

            cuisineEmailMap.put("cuisine", cuisineEmailPair.getCuisine());
            cuisineEmailMap.put("email", cuisineEmailPair.getEmail());

            String messageReceiptHandle = message.receiptHandle();
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(messageReceiptHandle)
                    .build());
        }
        System.out.println(cuisineEmailMap.get("cuisine"));
    }

    public static void main(String[] args) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy");

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

        receiveSQSMessages("https://sqs.us-east-2.amazonaws.com/508829990173/userRequest", sqsClient);
    }
}
