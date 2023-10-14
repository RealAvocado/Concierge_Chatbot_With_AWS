package Lambda2;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.*;

public class DynamoDbQueryTest {

    public static DynamoDbClient initializeDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy")))
                .build();
    }

    public static List<Map<String, String>> getRestaurantsInfoBasedOnAllIds(List<String> restaurantsIdList, String tableName, String partitionKeyName, DynamoDbClient ddbClient) {
        List<Map<String, String>> restaurantsInfoMapList = new ArrayList<>();
        for (String restaurantId : restaurantsIdList) {
            restaurantsInfoMapList.add(getSingleRestaurantInfoBasedOnId(tableName, partitionKeyName, restaurantId, ddbClient));
        }
        return restaurantsInfoMapList;
    }

    /**
     * @return Example: {key: name, value: Trim Dim; key location, value: 323 W 96th St; key: cuisine, value: chinese}
     */
    public static Map<String, String> getSingleRestaurantInfoBasedOnId(String tableName, String partitionKeyName, String businessID, DynamoDbClient ddbClient) {
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
        List<String> restaurantsIdList = Arrays.asList("Fo4ERbuHK-AmLON6Zao8aQ", "yzCS-anGZRWRpvM7gG_N8g", "P-gHeX8dDeZrq64l3DWMsw", "g38FaHmp6GmwPOzxi_xztQ");
        DynamoDbClient ddbClient = initializeDynamoDbClient();
        List<Map<String, String>> restaurantsInfoMapList = getRestaurantsInfoBasedOnAllIds(
                restaurantsIdList,
                "yelp-restaurants",
                "BusinessID",
                ddbClient);

        for (Map<String, String> restaurantsInfoMap : restaurantsInfoMapList) {
            System.out.println(
                    "Name: " + restaurantsInfoMap.get("Name") + " " +
                            // some "Phone" field is stored as <empty> in DynamoDB, which means an empty String object
                    "Phone: " + ((restaurantsInfoMap.get("Phone").isEmpty()) ? "Phone is not available" : restaurantsInfoMap.get("Phone")) + " " +
                    "Website: " + restaurantsInfoMap.get("Website") + " " +
                    "Category: " + restaurantsInfoMap.get("Category") + " " +
                    "Location: " + restaurantsInfoMap.get("Location") + " " +
                    "Zip: " + restaurantsInfoMap.get("Zip") + " " +
                    "Rating: " + restaurantsInfoMap.get("Rating"));
        }

    }
}
