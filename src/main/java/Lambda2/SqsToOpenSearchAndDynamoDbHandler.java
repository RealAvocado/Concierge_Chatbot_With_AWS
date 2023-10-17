package Lambda2;

import Lambda2.OpenSearchResponseEntity.IndexPattern;
import Lambda2.SqsMessageBodyEntity.CuisineEmailPair;
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
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import javax.mail.MessagingException;
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
                .create("myKeyId", "mySecretKey");

        // receive and process the SQS message
        Map<String, String> cuisineEmailMap =
                getCuisineAndEmailFromSQS("https://sqs.us-east-2.amazonaws.com/508829990173/userRequest",awsCredentials);
        String cuisine;
        if (cuisineEmailMap.isEmpty()){
            return null;
        } else {
            cuisine = cuisineEmailMap.get("cuisine");
        }

        // query the OpenSearch for restaurants ID based on the cuisine received from SQS
        List<String> restaurantIdList;
        try {
            restaurantIdList = getIdBasedOnCuisineFromOpenSearch(cuisine);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // get complete restaurants information from DynamoDB based on ID from OpenSearch
        DynamoDbClient ddbClient = initializeDynamoDbClient(awsCredentials);

        //query DynamoDB
        List<Map<String, String>> restaurantsInfoMapList = getRestaurantsInfoBasedOnAllIds(
                restaurantIdList,
                "yelp-restaurants",
                "BusinessID",
                ddbClient);

        // set up SES client
        SesClient sesClient = SesClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();

        String emailContent = getEmailContent(restaurantsInfoMapList, cuisine);
        String recipientEmail = cuisineEmailMap.get("email");
        try {
            sendEmail(sesClient, "jdcomputing123@gmail.com", recipientEmail, "Restaurants Recommendation", emailContent);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return "Recommendation successfully sent";
    }

    public Map<String, String> getCuisineAndEmailFromSQS(String queueUrl, AwsBasicCredentials awsCredentials) {
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

        for (software.amazon.awssdk.services.sqs.model.Message message : sqsClient.receiveMessage(receiveMessageRequest).messages()) {
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
        String username = "myOpenSearchUserName";
        String password = "myPassword";
        String credentials = username + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        //get response from OpenSearch
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://search-opensearch-lunihu3xwkc67ywkmrnwdeb7le.us-east-2.es.amazonaws.com/restaurants/_search?q=Cuisine:" + cuisine))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Basic " + base64Credentials)
                .build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        //System.out.println(httpResponse.body());

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

        // shuffle the list so that customers can get random suggestions
        Collections.shuffle(indexPatternList);

        List<String> restaurantIdList = new ArrayList<>();
        for (int i = 0; i < Math.min(indexPatternList.size(), 5); i++) {
            IndexPattern indexPattern = indexPatternList.get(i);
            restaurantIdList.add(indexPattern.getSource().getId());
        }

        return restaurantIdList;
    }

    public DynamoDbClient initializeDynamoDbClient(AwsBasicCredentials awsCredentials) {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();
    }

    public List<Map<String, String>> getRestaurantsInfoBasedOnAllIds(List<String> restaurantsIdList, String tableName, String partitionKeyName, DynamoDbClient ddbClient) {
        List<Map<String, String>> restaurantsInfoMapList = new ArrayList<>();
        for (String restaurantId : restaurantsIdList) {
            restaurantsInfoMapList.add(getSingleRestaurantInfoBasedOnId(tableName, partitionKeyName, restaurantId, ddbClient));
        }
        return restaurantsInfoMapList;
    }

    public Map<String, String> getSingleRestaurantInfoBasedOnId(String tableName, String partitionKeyName, String businessID, DynamoDbClient ddbClient) {
        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":partitionKeyValue", AttributeValue.builder().s(businessID).build());

        // query expression
        String partitionKeyExpression = partitionKeyName + " = :partitionKeyValue";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(partitionKeyExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        // return this as result
        Map<String, String> restaurantsInfoMap = new HashMap<>();

        try {
            QueryResponse response = ddbClient.query(queryRequest);
            response.items().stream().flatMap(item -> item.entrySet().stream()).forEach(entry -> {
                String key = entry.getKey();
                AttributeValue value = entry.getValue();
                // don't need to send these two value to customers
                if (Objects.equals(key, "InsertedTime") || Objects.equals(key, "BusinessID")) {
                    return;
                }
                if (Objects.equals(key, "Category")) {
                    List<AttributeValue> categoryList = value.m().get("Items").l();
                    //System.out.print(key + ": ");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < categoryList.size(); i++) {
                        AttributeValue attributeValue = categoryList.get(i);
                        if (i < categoryList.size() - 1) {
                            stringBuilder.append(attributeValue.s()).append(", ");
                        } else {
                            stringBuilder.append(attributeValue.s());
                        }
                        //System.out.print(attributeValue.s() + ", ");
                    }
                    //System.out.println();
                    restaurantsInfoMap.put(key, stringBuilder.toString());

                } else {
                    //System.out.println(key + ": " + value.s());
                    restaurantsInfoMap.put(key, value.s());
                }
            });

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return restaurantsInfoMap;
    }

    public String getEmailContent(List<Map<String, String>> restaurantsInfoMapList, String cuisine) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dear customer,\n\nHere are my restaurant suggestions for cuisine type: ").append(cuisine).append("\n\n");
        for (Map<String, String> restaurantsInfoMap : restaurantsInfoMapList) {
            stringBuilder
                    .append("Restaurant: ")
                    .append(restaurantsInfoMap.get("Name")).append("\n")
                    .append("Location: ")
                    .append(restaurantsInfoMap.get("Location")).append("\n")
                    .append("Cuisine: ")
                    .append(restaurantsInfoMap.get("Category")).append("\n")
                    .append("Rating: ")
                    .append(restaurantsInfoMap.get("Rating")).append("\n")
                    .append("Phone: ")
                    .append(((restaurantsInfoMap.get("Phone").isEmpty()) ? "Not available" : restaurantsInfoMap.get("Phone"))).append("\n")
                    .append("Website: ")
                    .append(restaurantsInfoMap.get("Website")).append("\n\n");

        }
        stringBuilder.append("Best regards,").append("\n").append("Your Dining Concierge");
        return stringBuilder.toString();
    }

    public void sendEmail(
            SesClient client,
            String sender,
            String recipient,
            String subject,
            String bodyText
    ) throws MessagingException {
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(recipient).build())
                .message(Message.builder()
                        .body(Body.builder().text(Content.builder().data(bodyText).build()).build())
                        .subject(Content.builder().data(subject).build())
                        .build())
                .source(sender)
                .build();

        try {
            System.out.println("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");
            client.sendEmail(emailRequest);
            System.out.println("Email sent successfully.");

        } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SqsToOpenSearchAndDynamoDbHandler sqsToOpenSearchAndDynamoDbHandler = new SqsToOpenSearchAndDynamoDbHandler();
        Void v = null;
        sqsToOpenSearchAndDynamoDbHandler.handleRequest(v, null);
    }

}
