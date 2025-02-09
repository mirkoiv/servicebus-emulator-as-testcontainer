package example.testcontainers.servicebusemulator.fakes;

public interface MessageRepository {
    void save(String message);

    void clear();

    int count();

    String get(int index);
}
