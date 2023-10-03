package DataScraping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DataScraping.RestaurantEntity.Restaurant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class YelpToDynamoDB {

    private static Map<String, AttributeValue> restaurantDataToMap(Restaurant restaurant) {
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("BusinessID", AttributeValue.builder().s(restaurant.getId()).build());
        itemMap.put("Name", AttributeValue.builder().s(restaurant.getName()).build());
        itemMap.put("Category", AttributeValue.builder().s(restaurant.getCategories().toString()).build());
        itemMap.put("Location", AttributeValue.builder().s(restaurant.getLocation().getAddress1()).build());
        itemMap.put("Coordinates", AttributeValue.builder().s(restaurant.getCoordinates().toString()).build());
        itemMap.put("NumberOfReviews", AttributeValue.builder().s(String.valueOf(restaurant.getReviewCount())).build());
        itemMap.put("Rating", AttributeValue.builder().s(String.valueOf(restaurant.getRating())).build());
        itemMap.put("Zip", AttributeValue.builder().s(restaurant.getLocation().getZipCode()).build());
        return itemMap;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int offset = 0;
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.yelp.com/v3/businesses/search?location=manhattan&sort_by=best_match&limit=50&offset="+offset))
                .header("accept", "application/json")
                .header("Authorization", "Bearer NO66ZvfaUhYV5IDrxMeQKhWO_97B2qafjsVek8DDDuu2JKSUh0pPv0BSOREVPOAMk2xUQwQDVPCdkLVvbEVWdNn7iEvn6UH5VzChj_rVYV_1TurNTUEK4VfcX-EUZXYx")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        //System.out.println(response.body());

        int startIndex = httpResponse.body().indexOf('[');
        int endIndex = httpResponse.body().lastIndexOf(']');
        String bodyJSON = httpResponse.body().substring(startIndex, endIndex + 1);
        JSONArray arr = JSON.parseArray(bodyJSON);
        List<Restaurant> restaurantList = new ArrayList<>();

        for (int i = 0; i < arr.size(); i++) {
            ObjectMapper mapper = new ObjectMapper();
            try{
                Restaurant restaurant = mapper.readValue(arr.getString(i), Restaurant.class);
                restaurantList.add(restaurant);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        for (Restaurant restaurant : restaurantList) {
            System.out.println("Business ID: " + restaurant.getId() +
                    "; Restaurant Name: " + restaurant.getName() +
                    "; Category: " + restaurant.getCategories() +
                    "; Location: " + restaurant.getLocation().getAddress1() +
                    "; Coordinates: " + restaurant.getCoordinates() +
                    "; Number Of Reviews: " + restaurant.getReviewCount() +
                    "; Rating: " + restaurant.getRating() +
                    "; Zip: " + restaurant.getLocation().getZipCode());
        }

        DynamoDbClient ddbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000"))
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy")))
                .build();

        for (Restaurant restaurant : restaurantList) {
            PutItemRequest insertRequest = PutItemRequest.builder()
                    .tableName("yelp-restaurants")
                    .item(restaurantDataToMap(restaurant))
                    .build();

            try {
                PutItemResponse insertResponse = ddbClient.putItem(insertRequest);
                System.out.println("yelp-restaurants" +" was successfully updated. The request id is "+insertResponse.responseMetadata().requestId());

            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", "yelp-restaurants");
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
                System.exit(1);
            } catch (DynamoDbException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}
