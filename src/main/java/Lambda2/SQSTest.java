package Lambda2;

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
//    private static void processSQS() {
//        // set up SQS
//        try {
//            AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//            String queueUrl = "https://sqs.us-east-2.amazonaws.com/508829990173/userRequest";
//
//            // receive the message from SQS
//            List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
//
////            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
////            receiveMessageRequest.setMaxNumberOfMessages(1);
////            ReceiveMessageResult messages = sqs.receiveMessage(receiveMessageRequest);
//
//            for (Message message : messages) {
//                Map<String, String> attributes = message.getAttributes();
//                String cuisine = attributes.get("Cuisine");
//                String email = attributes.get("Email");
//                System.out.println(cuisine);
//            }
//        } catch (Exception e) {
//            // Handle any exceptions, e.g., logging, error reporting, etc.
//            e.printStackTrace();
//        }
//    }
    public static void receiveSQSMessages(String queueUrl, SqsClient sqsClient) {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().
                queueUrl(queueUrl).
                maxNumberOfMessages(1).
                build();
//        if (!sqsClient.receiveMessage(receiveMessageRequest).messages().isEmpty()){
//            Message message = sqsClient.receiveMessage(receiveMessageRequest).messages().get(0);
//
//            System.out.println(message.messageId());
//            System.out.println(message.body());
//            String messageReceiptHandle = message.receiptHandle();
//            sqsClient.deleteMessage(DeleteMessageRequest.builder()
//                    .queueUrl(queueUrl)
//                    .receiptHandle(messageReceiptHandle)
//                    .build());
//        }
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
