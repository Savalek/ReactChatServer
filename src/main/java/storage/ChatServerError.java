package storage;

import io.javalin.http.HttpResponseException;

import java.util.Collections;
import java.util.Map;

public class ChatServerError extends HttpResponseException {

    public ChatServerError(int status, String message, Map<String, String> details) {
        super(status, message, details);
    }

    public ChatServerError(int status, String message) {
        super(status, message, Collections.emptyMap());
    }
}
