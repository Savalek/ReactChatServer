import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import lombok.SneakyThrows;
import org.eclipse.jetty.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

public class ChatServer {

//    private static final User SAVALEK = new User("Savalek", "#000c84");
//    private static final User LIKA = new User("Lika", "#b400b3");

    private static final Map<Integer, User> users = new HashMap<Integer, User>() {{
//        put(SAVALEK.getId(), SAVALEK);
//        put(LIKA.getId(), LIKA);
    }};


    private static final List<Message> messages = new ArrayList<Message>() {{
//        add(new Message(SAVALEK.getId(), "Hello, Lika!"));
//        add(new Message(LIKA.getId(), "Hello, Savalek!"));
//        add(new Message(SAVALEK.getId(), "How are you?"));
//        add(new Message(LIKA.getId(), "I'm fine. how are you?"));
    }};

    @SneakyThrows
    private static void printLog() {
        while (true) {
            String userNameList = users.values()
                    .stream()
                    .map(User::getName)
                    .collect(Collectors.joining(", "));
            System.out.printf("\rMsg count: %5d. Users: %s", messages.size(), userNameList);
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) {
        System.out.println("Init web");
        WebStaticProject webStaticProject = new WebStaticProject("https://github.com/Savalek/ReactLearn", "./web");
        webStaticProject.cloneMasterTo();
        System.out.println("Web loaded");

        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
            config.addStaticFiles(webStaticProject.getAbsolutePath() + "/Chat", Location.EXTERNAL);
        }).start(80);

        app.get("/messages", ctx -> ctx.json(messages));
        app.put("/message", ChatServer::addNewMessage);

        app.get("/users", ctx -> ctx.json(users.values()));
        app.post("/users/getUserIdByName", ChatServer::getUserIdByName);

        app.get("/user/:id", ChatServer::getUserById);
        app.put("/user", ChatServer::addNewUser);

        new Thread(ChatServer::printLog).start();
    }

    private static void addNewMessage(Context ctx) {
        JsonObject json = new Gson().fromJson(ctx.body(), JsonObject.class);
        int userId = Integer.parseInt(json.get("userId").getAsString());
        String text = json.get("text").getAsString();
        checkUserId(userId);
        Message message = new Message(userId, text);
        messages.add(message);
        ctx.json(message.getId());
    }

    private static void checkUserId(int userId) {
        for (User user : users.values()) {
            if (user.getId() == userId) {
                return;
            }
        }
        new ErrorTrigger(HttpStatus.NOT_FOUND_404, "User with id '" + userId + "' does not exist.").doThrow();
    }

    private static void getUserIdByName(Context ctx) {
        String name = ctx.body();
        Optional<User> user = users.values().stream()
                .filter(u -> u.getName().equals(name))
                .findAny();
        if (user.isPresent()) {
            ctx.json(user.get().getId());
        } else {
            new ErrorTrigger(HttpStatus.NOT_FOUND_404, "User with name '" + name + "' does not exist.").doThrow();
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
