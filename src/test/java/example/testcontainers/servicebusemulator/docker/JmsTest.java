package example.testcontainers.servicebusemulator.docker;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import example.testcontainers.servicebusemulator.fakes.FakeMessageRepository;
import example.testcontainers.servicebusemulator.fakes.JmsListenerService;
import example.testcontainers.servicebusemulator.fakes.MessageRepository;
import jakarta.jms.ConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("docker-jms")
public class JmsTest extends AbstractServiceBusContainerDockerCompose {

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void jmsListenerTest() {

        senderClient.sendMessage(new ServiceBusMessage("Test Message"));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                            assertThat(messageRepository.count()).isEqualTo(1);
                            assertThat(messageRepository.get(0)).isEqualTo("Test Message");
                        }
                );
    }

    @TestConfiguration
    static class JmsConfiguration {

        @Bean
        public MessageRepository messageRepository() {
            return new FakeMessageRepository();
        }

        @Bean
        public JmsListenerService jmsListenerService(MessageRepository messageRepository) {
            return new JmsListenerService(messageRepository);
        }

        private static final String AMQP_URI_FORMAT = "amqp://localhost:%s";

        @Bean @Primary
        public ConnectionFactory connectionFactory(@Value("${emulator-ampq-port}") String port) {
            String remoteUri = String.format(AMQP_URI_FORMAT, port);
            JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
            jmsConnectionFactory.setRemoteURI(remoteUri);
            jmsConnectionFactory.setUsername("RootManageSharedAccessKey");
            jmsConnectionFactory.setPassword("SAS_KEY_VALUE");
            return jmsConnectionFactory;
        }
    }
}
