import io.javalin.http.HttpResponseException;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class ErrorTrigger {

    private final int status;
    private final String message;
    private final Map<String, String> details;

    public ErrorTrigger(int status, String message) {
        this(status, message, Collections.emptyMap());
    }

    public void doThrow() {
        throw new HttpResponseException(status, message, details);
    }
}
