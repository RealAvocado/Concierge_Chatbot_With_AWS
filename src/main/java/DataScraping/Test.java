package DataScraping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
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
    }
}
