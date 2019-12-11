import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Message {
    private final static AtomicInteger idCounter = new AtomicInteger(0);

    private final int id = idCounter.getAndIncrement();

    private final int userId;
    private final String text;
}
