import io.javalin.Javalin;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public class ChatServer {


    private static final User SAVALEK = new User("Savalek", "#000c84");
    private static final User LIKA = new User("Lika", "#b400b3");

    private static final List<Message> messages = new ArrayList<>();

    private static final List<Message> demoMessages = new ArrayList<Message>() {{
        add(new Message(SAVALEK, "Hello, Lika!"));
        add(new Message(LIKA, "Hello, Savalek!"));
        add(new Message(SAVALEK, "How are you?"));
        add(new Message(LIKA, "I'm fine. how are you?"));
        add(new Message(SAVALEK, "Me too"));
        add(new Message(LIKA, "."));
    }};

    static {
        new Thread(ChatServer::simulateChat).start();
    }

    @SneakyThrows
    private static void simulateChat() {
        while (true) {
            ArrayList<Message> tempMessages = new ArrayList<>(ChatServer.demoMessages);
            while (!tempMessages.isEmpty()) {
                Message message = tempMessages.remove(0);
                ChatServer.messages.add(message);
                Thread.sleep(2500);
            }
            Thread.sleep(10000);
        }
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
        }).start(7000);

        app.get("/message", ctx -> ctx.json(messages));
        app.put("/message", ctx -> messages.add(ctx.bodyAsClass(Message.class)));
    }
}
