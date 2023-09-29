package Lambda0;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ChatBotHandler implements RequestHandler<BotRequest, BotResponse> {
    @Override
    public BotResponse handleRequest(BotRequest input, Context context) {
        BotResponse response = new BotResponse();
        Message message = new Message();
        message.setType("text");
        UnstructuredMessage unstructuredMessage = new UnstructuredMessage();
        unstructuredMessage.setText("I’m still under development. Please come back later.");
        message.setUnstructured(unstructuredMessage);
        response.getMessages().add(message);

        return response;
    }
}
