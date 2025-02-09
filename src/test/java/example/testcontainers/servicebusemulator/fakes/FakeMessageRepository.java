package example.testcontainers.servicebusemulator.fakes;

import java.util.ArrayList;
import java.util.List;

public class FakeMessageRepository implements MessageRepository {
    private final List<String> list = new ArrayList<>();

    public void save(String message) {
        list.add(message);
    }

    public void clear() {
        list.clear();
    }

    public int count() {
        return list.size();
    }

    public String get(int index) {
        return list.get(index);
    }
}
