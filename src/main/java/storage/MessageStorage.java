package storage;

import core.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageStorage {

    private final List<Message> messages = new ArrayList<>();

    public List<Message> messages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
