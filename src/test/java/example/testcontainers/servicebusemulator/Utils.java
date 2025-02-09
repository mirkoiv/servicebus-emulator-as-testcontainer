package example.testcontainers.servicebusemulator;

import java.time.Duration;

public class Utils {
    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
