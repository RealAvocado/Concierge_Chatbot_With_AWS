package DataScraping;

import Lambda2.OpenSearchResponseEntity.IndexPattern;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Test {

    private static void queryOpenSearch() throws IOException, InterruptedException {
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

        System.out.println(restaurantIdList);
    }
    public static void main(String[] args) throws IOException, InterruptedException {

    }
}
