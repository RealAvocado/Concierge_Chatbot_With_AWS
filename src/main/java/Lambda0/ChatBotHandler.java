package Lambda0;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClient;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.amazonaws.services.lexruntime.model.PostTextResult;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBotHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) event.get("messages");

        String bot_response_message = "Please Try again!";

        if (messages != null && !messages.isEmpty()) {
            Map<String, Object> message = messages.get(0);
            Map<String, Object> unstructured = (Map<String, Object>) message.get("unstructured");

            String data = (String) unstructured.get("text");
            String regionName = "us-east-1";
            Regions region = Regions.fromName(regionName);
            AmazonLexRuntime client = AmazonLexRuntimeClientBuilder.standard()
                    .withRegion(region)
                    .build();

            PostTextRequest lexRequest = new PostTextRequest()
                    .withBotName("ChatBot")
                    .withBotAlias("dining_bot")
                    .withUserId("test")
                    .withInputText(data);

            PostTextResult lexResponse = client.postText(lexRequest);
            bot_response_message = lexResponse.getMessage();
        }

        Map<String, Object> message = new HashMap<>();
        Map<String, Object> unstructuredMessage = new HashMap<>();
        unstructuredMessage.put("id", "1");
        unstructuredMessage.put("text", bot_response_message);
        unstructuredMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));

        message.put("type", "unstructured");
        message.put("unstructured", unstructuredMessage);

        response.put("messages", List.of(message));

        return response;
    }
}
