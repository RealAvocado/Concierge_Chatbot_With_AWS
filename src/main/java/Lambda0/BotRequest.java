package Lambda0;

import java.util.List;

public class BotRequest {
    private List<Message> messages;

    public List<Message> getMessage(){
        return messages;
    }

    public void setMessages(List<Message> messages){
        this.messages = messages;
    }
}
