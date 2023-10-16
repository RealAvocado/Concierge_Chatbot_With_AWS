package DataScraping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

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

    private static void writeToBulkFileInJson(String content) {
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
                cuisineStr.append("\"").append(category.getTitle()).append("\",").append("\"").append(category.getAlias()).append("\"");
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
            if (!category.getTitle().equalsIgnoreCase(category.getAlias())) {
                categoryTitleAndAliasList.add(category.getAlias());
            }
        }

        categoryAttribute.put("Items", AttributeValue.builder().l(categoryTitleAndAliasList.stream().map(value -> AttributeValue.builder().s(String.valueOf(value)).build()).toArray(AttributeValue[]::new)).build());

        itemMap.put("BusinessID", AttributeValue.builder().s(restaurant.getId()).build());
        if (restaurant.getName() != null) {
            itemMap.put("Name", AttributeValue.builder().s(restaurant.getName()).build());
        }
        itemMap.put("Category", AttributeValue.builder().m(categoryAttribute).build());
        if (restaurant.getLocation().getDisplayAddress() != null) {
            itemMap.put("Location", AttributeValue.builder().s(restaurant.getLocation().getDisplayAddress().toString()).build());
        }
        if (restaurant.getCoordinates() != null) {
            itemMap.put("Coordinates", AttributeValue.builder().s(restaurant.getCoordinates().toString()).build());
        }

        itemMap.put("NumberOfReviews", AttributeValue.builder().s(String.valueOf(restaurant.getReviewCount())).build());
        itemMap.put("Rating", AttributeValue.builder().s(String.valueOf(restaurant.getRating())).build());
        if (restaurant.getLocation().getZipCode() != null) {
            itemMap.put("Zip", AttributeValue.builder().s(restaurant.getLocation().getZipCode()).build());
        }
        itemMap.put("InsertedTime", AttributeValue.builder().s(localDateTime.toString()).build());
        if (restaurant.getDisplayPhone() != null) {
            itemMap.put("Phone", AttributeValue.builder().s(restaurant.getDisplayPhone()).build());
        }
        if (restaurant.getUrl() != null) {
            itemMap.put("Website", AttributeValue.builder().s(restaurant.getUrl()).build());
        }
        return itemMap;
    }

    private static void insertDataIntoDynamoDB(List<Restaurant> restaurantList, DynamoDbClient ddbClient, String tableName) {
        for (Restaurant restaurant : restaurantList) {
            LocalDateTime localDateTime = LocalDateTime.now();

            try {
                PutItemRequest insertRequest = PutItemRequest.builder().tableName(tableName).item(restaurantDataToMap(restaurant, localDateTime)).build();
                PutItemResponse insertResponse = ddbClient.putItem(insertRequest);
                System.out.println(tableName + " was successfully updated. The request id is " + insertResponse.responseMetadata().requestId());

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

    private static void printRestaurantTest(List<Restaurant> restaurantList) {
        for (Restaurant restaurant : restaurantList) {
            System.out.println("Business ID: " + restaurant.getId() + "; Restaurant Name: " + restaurant.getName() + "; Category: " + restaurant.getCategories() + "; Location: " + restaurant.getLocation().getAddress1() + "; Coordinates: " + restaurant.getCoordinates() + "; Number Of Reviews: " + restaurant.getReviewCount() + "; Rating: " + restaurant.getRating() + "; Zip: " + restaurant.getLocation().getZipCode());
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy")))
                .build();


        int openSearchIndexId = 1; // id for OpenSearch index

        List<String> restaurantCategoryList = Arrays.asList(
                "african", "newamerican", "tradamerican", "asianfusion", "australian", "bbq",
                "breakfast_brunch", "buffets", "burgers", "cafes", "caribbean", "chickenshop",
                "chicken_wings", "chinese", "hotdogs", "fishnchips", "french", "german",
                "hawaiian", "hotdog", "indpak", "italian", "japanese", "korean",
                "mexican", "mideastern", "noodles", "pizza", "salad", "sandwiches",
                "seafood", "soup", "spanish", "steak", "sushi", "thai",
                "turkish", "vegan", "vietnamese");
        
        for (String cuisine : restaurantCategoryList) {

            int totalLoopCountInThisRequest = 1;
            int offset = 0; // offset of data from http request

            for (int i = 0; i < totalLoopCountInThisRequest; i++) {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.yelp.com/v3/businesses/search?location=manhattan&categories=" + cuisine + "&sort_by=rating&limit=50&offset=" + offset))
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer NO66ZvfaUhYV5IDrxMeQKhWO_97B2qafjsVek8DDDuu2JKSUh0pPv0BSOREVPOAMk2xUQwQDVPCdkLVvbEVWdNn7iEvn6UH5VzChj_rVYV_1TurNTUEK4VfcX-EUZXYx")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

                // automatically get total number of records in the current HTTP request at the first page
                if (i == 0) {
                    String target1 = "\"total\":", target2 = "\"region\"";
                    int indexTarget1 = httpResponse.body().indexOf(target1);
                    int indexTarget2 = httpResponse.body().indexOf(target2);
                    int totalNumberOfRecords = Integer.parseInt(httpResponse.body().substring(indexTarget1 + target1.length() + 1, indexTarget2 - 2));
                    if (totalNumberOfRecords % 50 == 0) {
                        totalLoopCountInThisRequest = Math.min((totalNumberOfRecords / 50), 10);
                    } else {
                        totalLoopCountInThisRequest = Math.min((totalNumberOfRecords / 50) + 1, 10);
                    }
                    System.out.println(totalLoopCountInThisRequest);
                }


                int startIndex = httpResponse.body().indexOf('[');
                int endIndex = httpResponse.body().lastIndexOf(']');
                String bodyJSON = httpResponse.body().substring(startIndex, endIndex + 1);
                JSONArray arr = JSON.parseArray(bodyJSON);
                List<Restaurant> restaurantList = new ArrayList<>();

                for (int j = 0; j < arr.size(); j++) {
                    ObjectMapper mapper = new ObjectMapper();
                    Restaurant restaurant = new Restaurant();
                    try {
                        restaurant = mapper.readValue(arr.getString(j), Restaurant.class);
                        restaurantList.add(restaurant);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //write data into bulk file
                    String IdAndCuisineInIndex = "{ \"index\" : { \"_index\": \"restaurants\", \"_id\" : \"" + openSearchIndexId++ + "\" } }\n" + "{\"ID\": \"" + restaurant.getId() + "\", \"Cuisine\": [" + getCuisineString(restaurant) + "]}\n";
                    writeToBulkFileInJson(IdAndCuisineInIndex);
                }

                try {
                    insertDataIntoDynamoDB(restaurantList, ddbClient, "yelp-restaurants");
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }

                offset += 50;
            }
        }
    }
}
