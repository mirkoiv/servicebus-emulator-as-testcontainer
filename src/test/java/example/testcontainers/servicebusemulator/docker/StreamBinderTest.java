package example.testcontainers.servicebusemulator.docker;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import example.testcontainers.servicebusemulator.fakes.MessageRepository;
import example.testcontainers.servicebusemulator.fakes.FakeMessageRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.function.Consumer;

import static com.azure.spring.messaging.AzureHeaders.CHECKPOINTER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("docker-stream-binder")
public class StreamBinderTest extends AbstractServiceBusContainerDockerCompose {

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void contextLoadJms() {

        senderClient.sendMessage(new ServiceBusMessage("Test Message"));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(messageRepository.count()).isEqualTo(1);
                    assertThat(messageRepository.get(0)).isEqualTo("Test Message");
                        }
                );
    }

    @TestConfiguration
    static class StreamBinderConfiguration {
        private static final Logger LOGGER = LoggerFactory.getLogger("consumer");

        @Bean
        public MessageRepository messageRepository() {
            return new FakeMessageRepository();
        }

        @Bean
        public Consumer<Message<String>> consume(MessageRepository messageRepository) {
            return message -> {
                Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
                LOGGER.info("New message received: '{}'", message);
                messageRepository.save(message.getPayload());
                checkpointer.success()
                        .doOnSuccess(s -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
                        .doOnError(e -> LOGGER.error("Error found", e))
                        .block();
            };
        }
    }
}
