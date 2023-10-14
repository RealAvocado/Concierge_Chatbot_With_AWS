package Lambda2;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DynamoDbQueryTest {
    public static Map<String, String> getRestaurantsInfoBasedOnId(String businessID) {
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy")))
                .build();

        String tableName = "yelp-restaurants";
        String partitionKeyName = "BusinessID";

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

    public static void main(String[] args) {
        Map<String, String> restaurantsInfoMap = getRestaurantsInfoBasedOnId("FZpm4_fbd6P984foOUejWg");
        System.out.println(restaurantsInfoMap.get("Name"));
    }
}
