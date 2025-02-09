package example.testcontainers.servicebusemulator.fakes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

public class JmsListenerService {
    private final Logger log = LoggerFactory.getLogger(JmsListenerService.class);
    private final MessageRepository messageRepository;

    public JmsListenerService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @JmsListener(destination = "${spring.cloud.azure.servicebus.entity-name}", containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(byte[] messageBytes) {
        String message = new String(messageBytes);
        log.info("Received message from queue: {}", message);
        messageRepository.save(message);
    }
}
