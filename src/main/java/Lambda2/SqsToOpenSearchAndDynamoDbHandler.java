package Lambda2;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.elasticsearch.action.search.SearchRequest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.ses.SesClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * This lambda function is triggered by EventBridge Scheduler that runs every minute
 * This automates the queue worker Lambda to poll and process suggestion requests on its own.
 */
public class SqsToOpenSearchAndDynamoDbHandler implements RequestHandler<Void, String> {
    public String handleRequest(Void input, Context context) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy");

        // set up SQS
        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrl = "https://sqs.us-east-2.amazonaws.com/508829990173/userRequest";

        // set up DynamoDB client

        DynamoDbClient ddbClient = DynamoDbClient.builder()
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

        // set up SES client
        SesClient sesClient = SesClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

        // receive messages
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        receiveMessageRequest.setMaxNumberOfMessages(1);
        ReceiveMessageResult messages = sqs.receiveMessage(receiveMessageRequest);

        for (Message message : messages.getMessages()) {
            Map<String, String> attributes = message.getAttributes();
            String cuisine = attributes.get("Cuisine");
            String email = attributes.get("Email");
            sqs.deleteMessage(queueUrl, message.getReceiptHandle());
        }

        return "Received and processed messages";
    }

    public String getIdFromOpenSearch(OpenSearchClient openSearchClient, String cuisine) throws IOException, InterruptedException {
        String username = "opensearch-user";
        String password = "B5A7vEdxnRLXJhh-";
        String credentials = username + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://search-opensearch-lunihu3xwkc67ywkmrnwdeb7le.us-east-2.es.amazonaws.com/restaurants/_search?q=Cuisine:Bakeries"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Basic " + base64Credentials)
                .build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
        return httpResponse.body();
    }
}
