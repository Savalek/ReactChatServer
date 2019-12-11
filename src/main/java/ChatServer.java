import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {

    private static final User SAVALEK = new User("Savalek", "#000c84");
    private static final User LIKA = new User("Lika", "#b400b3");

    private static final Map<Integer, User> users = new HashMap<Integer, User>() {{
        put(SAVALEK.getId(), SAVALEK);
        put(LIKA.getId(), LIKA);
    }};


    private static final List<Message> messages = new ArrayList<Message>() {{
        add(new Message(SAVALEK.getId(), "Hello, Lika!"));
        add(new Message(LIKA.getId(), "Hello, Savalek!"));
        add(new Message(SAVALEK.getId(), "How are you?"));
        add(new Message(LIKA.getId(), "I'm fine. how are you?"));
        add(new Message(SAVALEK.getId(), "Me too"));
        add(new Message(LIKA.getId(), "."));
    }};

    static {
        new Thread(ChatServer::printLog).start();
    }

    @SneakyThrows
    private static void printLog() {
        Thread.sleep(2000);
        while (true) {
            String userNameList = users.values()
                    .stream()
                    .map(User::getName)
                    .collect(Collectors.joining(", "));
            System.out.printf("\rMsg count: %5d. Users: %s", messages.size(), userNameList);
            Thread.sleep(250);
        }
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
        }).start(7000);

        app.get("/messages", ctx -> ctx.json(messages));
        app.put("/message", ctx -> messages.add(ctx.bodyAsClass(Message.class)));

        app.get("/users", ctx -> ctx.json(users.values()));
        app.get("/user/:id", ChatServer::getUserById);
        app.put("/user", ChatServer::addNewUser);
    }

    private static void getUserById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.json(users.get(id));
    }

    private static void addNewUser(Context ctx) {
        JsonObject json = (JsonObject) new Gson().toJsonTree(ctx.body());
        String name = json.get("name").getAsString();
        String color = json.get("color").getAsString();
        User user = new User(name, color);
        users.put(user.getId(), user);
        ctx.json(user);
    }
}
