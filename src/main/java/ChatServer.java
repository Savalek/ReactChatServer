import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core.Message;
import core.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import org.eclipse.jetty.http.HttpStatus;
import storage.ChatServerError;
import storage.MessageStorage;
import storage.UserStorage;
import web.WebProject;

import java.util.*;

public class ChatServer {

    private static final String WEB_PROJECT_GIT_URL = "https://github.com/Savalek/ReactChat";
    private static final String WEB_BUILD_SUB_DIRECTORY = "./web";

    private static UserStorage userStorage;
    private static MessageStorage messageStorage;


    public static void main(String[] args) {
        WebProject webStaticProject = new WebProject(WEB_PROJECT_GIT_URL, WEB_BUILD_SUB_DIRECTORY);
        webStaticProject.buildWebProject();

        userStorage = new UserStorage();
        messageStorage = new MessageStorage();

        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
            config.addStaticFiles(webStaticProject.getStaticFilesFolderPath(), Location.EXTERNAL);
        }).start(7000);

        app.get("/messages", ctx -> ctx.json(messageStorage.messages()));
        app.get("/user/:id", ChatServer::getUserById);
        app.get("/users", ctx -> ctx.json(userStorage.allUsers()));
        app.put("/message", ChatServer::addNewMessage);
        app.put("/user", ChatServer::addNewUser);
        app.post("/users/getUserIdByName", ChatServer::getUserIdByName);
    }

    private static void addNewMessage(Context ctx) {
        JsonObject json = new Gson().fromJson(ctx.body(), JsonObject.class);
        int userId = Integer.parseInt(json.get("userId").getAsString());
        String text = json.get("text").getAsString();
        if (!userStorage.contains(userId)) {
            throw new ChatServerError(HttpStatus.NOT_FOUND_404, "core.User with id '" + userId + "' does not exist.");
        }
        Message message = new Message(userId, text);
        messageStorage.addMessage(message);
        ctx.json(message.id());
    }

    private static void getUserIdByName(Context ctx) {
        String name = ctx.body();
        Optional<User> user = userStorage.allUsers().stream()
                .filter(u -> u.name().equals(name))
                .findAny();
        if (user.isPresent()) {
            ctx.json(user.get().id());
        } else {
            throw new ChatServerError(HttpStatus.NOT_FOUND_404, "core.User with name '" + name + "' does not exist.");
        }
    }

    private static void getUserById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (!userStorage.contains(id)) {
            throw new ChatServerError(HttpStatus.NOT_FOUND_404, "core.User with id '" + id + "' does not exist.");
        }
        ctx.json(userStorage.user(id));
    }

    private static void addNewUser(Context ctx) {
        JsonObject json = new Gson().fromJson(ctx.body(), JsonObject.class);
        String name = json.get("name").getAsString();
        String color = json.get("color").getAsString();
        User user = new User(name, color);
        checkNewUserName(user.name());
        userStorage.addUser(user);
        ctx.json(user.id());
    }

    private static void checkNewUserName(String userName) {
        for (User user : userStorage.allUsers()) {
            if (user.name().equals(userName)) {
                throw new ChatServerError(HttpStatus.CONFLICT_409, "core.User with name '" + userName + "' already exists");
            }
        }
    }
}
