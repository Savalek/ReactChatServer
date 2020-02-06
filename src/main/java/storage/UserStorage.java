package storage;

import core.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final User SAVALEK = new User("Savalek", "#000c84");
    private static final User LIKA = new User("Lika", "#b400b3");

    private static final Map<Integer, User> users = new HashMap<Integer, User>() {{
        put(SAVALEK.id(), SAVALEK);
        put(LIKA.id(), LIKA);
    }};

    public void addUser(User user) {
        users.put(user.id(), user);
    }

    public Collection<User> allUsers() {
        return users.values();
    }

    public User user(int userId) {
        return users.get(userId);
    }

    public boolean contains(int userId) {
        return user(userId) != null;
    }
}
