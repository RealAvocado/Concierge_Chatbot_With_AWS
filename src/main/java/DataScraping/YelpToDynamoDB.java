package DataScraping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DataScraping.RestaurantEntity.Category;
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

    private static void writeToBulkFileInJson(String content){
        try {
            String filePath = "D:\\Letian Jiang\\Academic\\NYU MSCS Tandon\\23 Fall Classes\\CS-GY 9223 Cloud Computing\\assignment 1\\restaurants-json.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
            writer.write(content);
            writer.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static String getCuisineString(Restaurant restaurant) {
        StringBuilder cuisineStr = new StringBuilder();
        List<Category> categories = restaurant.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (!category.getTitle().equalsIgnoreCase(category.getAlias())) {
                cuisineStr.append("\"").append(category.getTitle()).append("\",")
                        .append("\"").append(category.getAlias()).append("\"");
            } else {
                cuisineStr.append("\"").append(category.getTitle()).append("\"");
            }
            if (i < categories.size() - 1) {
                cuisineStr.append(",");
            }
        }
        return cuisineStr.toString();
    }

    private static Map<String, AttributeValue> restaurantDataToMap(Restaurant restaurant, LocalDateTime localDateTime) {
        Map<String, AttributeValue> itemMap = new HashMap<>();

        // store "Category" attribute information as nested Map structure into DynamoDB
        // because a restaurant is matched to multiple categories
        Map<String, AttributeValue> categoryAttribute = new HashMap<>();
        List<String> categoryTitleAndAliasList = new ArrayList<>();
        for (Category category : restaurant.getCategories()) {
            categoryTitleAndAliasList.add(category.getTitle());
            if (!category.getTitle().equalsIgnoreCase(category.getAlias())){
                categoryTitleAndAliasList.add(category.getAlias());
            }
        }

        categoryAttribute.put("Items", AttributeValue.builder().l(categoryTitleAndAliasList.stream()
                .map(value -> AttributeValue.builder().s(String.valueOf(value)).build())
                .toArray(AttributeValue[]::new)).build());

        itemMap.put("BusinessID", AttributeValue.builder().s(restaurant.getId()).build());
        itemMap.put("Name", AttributeValue.builder().s(restaurant.getName()).build());
        itemMap.put("Category", AttributeValue.builder().m(categoryAttribute).build());
        itemMap.put("Location", AttributeValue.builder().s(restaurant.getLocation().getAddress1()).build());
        itemMap.put("Coordinates", AttributeValue.builder().s(restaurant.getCoordinates().toString()).build());
        itemMap.put("NumberOfReviews", AttributeValue.builder().s(String.valueOf(restaurant.getReviewCount())).build());
        itemMap.put("Rating", AttributeValue.builder().s(String.valueOf(restaurant.getRating())).build());
        itemMap.put("Zip", AttributeValue.builder().s(restaurant.getLocation().getZipCode()).build());
        itemMap.put("InsertedTime", AttributeValue.builder().s(localDateTime.toString()).build());
        return itemMap;
    }

    private static void insertDataIntoDynamoDB (List<Restaurant> restaurantList, DynamoDbClient ddbClient, String tableName) {
        for (Restaurant restaurant : restaurantList) {
            LocalDateTime localDateTime = LocalDateTime.now();
            PutItemRequest insertRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(restaurantDataToMap(restaurant,localDateTime))
                    .build();

            try {
                PutItemResponse insertResponse = ddbClient.putItem(insertRequest);
                System.out.println(tableName +" was successfully updated. The request id is "+insertResponse.responseMetadata().requestId());

            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
                System.exit(1);
            } catch (DynamoDbException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    private static void printRestaurantTest (List<Restaurant> restaurantList){
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
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy")))
                .build();

        int offset = 0; // offset of data from http request
        int openSearchIndexId = 1; // id for OpenSearch index

        for (int i = 0; i < 1; i++) {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.yelp.com/v3/businesses/search?location=manhattan&sort_by=rating&limit=50&offset="+offset))
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer NO66ZvfaUhYV5IDrxMeQKhWO_97B2qafjsVek8DDDuu2JKSUh0pPv0BSOREVPOAMk2xUQwQDVPCdkLVvbEVWdNn7iEvn6UH5VzChj_rVYV_1TurNTUEK4VfcX-EUZXYx")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            int startIndex = httpResponse.body().indexOf('[');
            int endIndex = httpResponse.body().lastIndexOf(']');
            String bodyJSON = httpResponse.body().substring(startIndex, endIndex + 1);
            JSONArray arr = JSON.parseArray(bodyJSON);
            List<Restaurant> restaurantList = new ArrayList<>();

            for (int j = 0; j < arr.size(); j++) {
                ObjectMapper mapper = new ObjectMapper();
                Restaurant restaurant = new Restaurant();
                try{
                    restaurant = mapper.readValue(arr.getString(j), Restaurant.class);
                    restaurantList.add(restaurant);
                }catch (Exception e){
                    e.printStackTrace();
                }

                //write data into bulk file
                String IdAndCuisineInIndex = "{ \"index\" : { \"_index\": \"restaurants\", \"_id\" : \"" + openSearchIndexId++ + "\" } }\n"
                        + "{\"ID\": \""+ restaurant.getId() + "\", \"Cuisine\": [" + getCuisineString(restaurant) + "]}\n";
                writeToBulkFileInJson(IdAndCuisineInIndex);
            }

            insertDataIntoDynamoDB(restaurantList, ddbClient, "yelp-restaurants");

            offset += 50;
        }
    }
}
