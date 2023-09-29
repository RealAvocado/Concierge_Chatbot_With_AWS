package Lambda0;

import java.util.ArrayList;
import java.util.List;

public class BotResponse {
    private List<Message> messages = new ArrayList<>();

    // Getter and setter for 'messages'
    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

