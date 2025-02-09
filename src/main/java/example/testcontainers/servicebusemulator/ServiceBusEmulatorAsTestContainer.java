package example.testcontainers.servicebusemulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class ServiceBusEmulatorAsTestContainer {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusEmulatorAsTestContainer.class, args);
    }

}
