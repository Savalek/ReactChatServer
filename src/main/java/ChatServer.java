import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.SneakyThrows;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
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
        app.post("/users/getUserIdByName", ChatServer::getUserIdByName);

        app.get("/user/:id", ChatServer::getUserById);
        app.put("/user", ChatServer::addNewUser);
    }

    private static void getUserIdByName(Context ctx) {
        String name = ctx.body();
        Optional<User> user = users.values().stream()
                .filter(u -> u.getName().equals(name))
                .findAny();
        if (user.isPresent()) {
            ctx.json(user.get().getId());
        } else {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static void getUserById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (!users.containsKey(id)) {
            new ErrorTrigger(HttpStatus.NOT_FOUND_404, "User with id '" + id + "' does not exist.").doThrow();
        }
        ctx.json(users.get(id));
    }

    private static void addNewUser(Context ctx) {
        JsonObject json = new Gson().fromJson(ctx.body(), JsonObject.class);
        String name = json.get("name").getAsString();
        String color = json.get("color").getAsString();
        User user = new User(name, color);
        checkUser(user);
        users.put(user.getId(), user);
        ctx.json(user.getId());
    }

    private static void checkUser(User user) {
        for (User value : users.values()) {
            if (value.getName().equals(user.getName())) {
                new ErrorTrigger(HttpStatus.CONFLICT_409, "User with name '" + user.getName() + "' already exists").doThrow();
            }
        }
    }
}
