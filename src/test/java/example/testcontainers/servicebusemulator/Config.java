package example.testcontainers.servicebusemulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record Config(
    @Value("${spring.cloud.azure.servicebus.entity-name}")
    String entityName,

    @Value("${spring.cloud.azure.servicebus.connection-string}")
    String emulatorConnectionString
) {

}
