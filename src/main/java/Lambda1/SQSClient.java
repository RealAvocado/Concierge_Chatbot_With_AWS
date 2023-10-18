package Lambda1;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSClient {
    private final String queueUrl;
    private final AmazonSQS sqs;

    public SQSClient(AmazonSQS sqs, String queueUrl){
        this.sqs = sqs;
        this.queueUrl = queueUrl;
    }

    public void sendMessage(String jsonMessage){
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(jsonMessage);
        sqs.sendMessage(sendMessageRequest);
    }
}
