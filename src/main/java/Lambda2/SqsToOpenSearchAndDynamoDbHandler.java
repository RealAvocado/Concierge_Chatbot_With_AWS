package Lambda2;

import Lambda2.OpenSearchResponseEntity.IndexPattern;
import Lambda2.SqsMessages.CuisineEmailPair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This lambda function is triggered by EventBridge Scheduler that runs every minute
 * This automates the queue worker Lambda to poll and process suggestion requests on its own.
 */
public class SqsToOpenSearchAndDynamoDbHandler implements RequestHandler<Void, String> {
    public String handleRequest(Void input, Context context) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials
                .create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy");

        // receive and process the SQS message
        Map<String, String> cuisineEmailMap =
                getCuisineAndEmailFromSQS("https://sqs.us-east-2.amazonaws.com/508829990173/userRequest",awsCredentials);
        String cuisine;
        if (cuisineEmailMap.isEmpty()){
            return null;
        } else {
            cuisine = cuisineEmailMap.get("Cuisine");
        }

        // query the OpenSearch for restaurants ID based on the cuisine received from SQS
        List<String> restaurantIdList;
        try {
            restaurantIdList = getIdBasedOnCuisineFromOpenSearch(cuisine);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // get complete restaurants information from DynamoDB
        List<String> restaurantsRecommendationList = getRestaurantsInfoBasedOnId(restaurantIdList, awsCredentials);

        // set up SES client
        SesClient sesClient = SesClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

        return "Recommendation successfully sent";
    }
    public static Map<String, String> getCuisineAndEmailFromSQS(String queueUrl, AwsBasicCredentials awsCredentials) {
        // set up SQS client
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

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

        return cuisineEmailMap;
    }

    public List<String> getIdBasedOnCuisineFromOpenSearch(String cuisine) throws IOException, InterruptedException {
        String username = "opensearch-user";
        String password = "B5A7vEdxnRLXJhh-";
        String credentials = username + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        //get response from OpenSearch
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://search-opensearch-lunihu3xwkc67ywkmrnwdeb7le.us-east-2.es.amazonaws.com/restaurants/_search?q=Cuisine:" + cuisine))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Basic " + base64Credentials)
                .build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());

        int startIndex = httpResponse.body().indexOf('[');
        int endIndex = httpResponse.body().lastIndexOf(']');
        String bodyJSON = httpResponse.body().substring(startIndex, endIndex + 1);
        JSONArray arr = JSON.parseArray(bodyJSON);
        List<IndexPattern> indexPatternList = new ArrayList<>();

        for (int j = 0; j < arr.size(); j++) {
            ObjectMapper mapper = new ObjectMapper();
            IndexPattern indexPattern;
            try{
                indexPattern = mapper.readValue(arr.getString(j), IndexPattern.class);
                indexPatternList.add(indexPattern);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        List<String> restaurantIdList = new ArrayList<>();
        for (IndexPattern indexPattern : indexPatternList) {
            restaurantIdList.add(indexPattern.getSource().getId());
        }

        return restaurantIdList;
    }

    public List<String> getRestaurantsInfoBasedOnId(List<String> restaurantIdList, AwsBasicCredentials awsCredentials) {
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

        String tableName = "yelp-restaurants";
        String partitionKeyName = "BusinessID";
        String businessID = "";

        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":partitionKeyValue", AttributeValue.builder().s(businessID).build());

        // query expression
        String partitionKeyExpression = partitionKeyName + " = :partitionKeyValue";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(partitionKeyExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            QueryResponse response = ddbClient.query(queryRequest);

            response.items().forEach(item -> {
                item.forEach((key, value) -> System.out.println(key + " " + value.s()));
            });

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }


        return null;
    }

    public static void main(String[] args) {

    }

}
