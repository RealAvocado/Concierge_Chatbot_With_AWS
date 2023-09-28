package DataScraping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class YelpToDynamoDB {
    public static void main(String[] args) throws IOException, InterruptedException {
        int offset = 0;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.yelp.com/v3/businesses/search?location=manhattan&categories=chinese&sort_by=best_match&limit=50&offset="+offset))
                .header("accept", "application/json")
                .header("Authorization", "Bearer NO66ZvfaUhYV5IDrxMeQKhWO_97B2qafjsVek8DDDuu2JKSUh0pPv0BSOREVPOAMk2xUQwQDVPCdkLVvbEVWdNn7iEvn6UH5VzChj_rVYV_1TurNTUEK4VfcX-EUZXYx")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

//        DynamoDbClient client = DynamoDbClient.builder()
//                .endpointOverride(URI.create("http://localhost:8000"))
//                // The region is meaningless for local DynamoDb but required for client builder validation
//                .region(Region.US_EAST_2)
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy")))
//                .build();
    }
}
