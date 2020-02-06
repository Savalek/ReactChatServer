package core;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@Accessors(fluent = true)
public class User {
    private final static AtomicInteger idCounter = new AtomicInteger(0);

    private final int id = idCounter.getAndIncrement();
    private final String name;
    private final String color;
}
