import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class User {
    private final static AtomicInteger idCounter = new AtomicInteger(0);

    private final int id = idCounter.getAndIncrement();

    private final String name;
    private final String color;
}
