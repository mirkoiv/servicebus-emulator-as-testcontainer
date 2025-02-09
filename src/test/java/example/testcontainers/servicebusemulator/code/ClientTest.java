package example.testcontainers.servicebusemulator.code;

import com.azure.messaging.servicebus.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("code-client")
public class ClientTest extends AbstractServiceBusContainerCodeCompose {

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusReceiverClient receiverClient;

    @Test
    void serviceBusReceiverClientTest() {
        senderClient.sendMessage(new ServiceBusMessage("Test Message"));

        Iterator<ServiceBusReceivedMessage> iterator = receiverClient.receiveMessages(1, Duration.of(1, ChronoUnit.SECONDS)).stream().iterator();

        await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                            assertThat(iterator.hasNext()).isTrue();
                            assertThat(iterator.next().getBody().toString()).isEqualTo("Test Message");
                        }
                );
    }
}
