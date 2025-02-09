package example.testcontainers.servicebusemulator.docker;

import example.testcontainers.servicebusemulator.EmulatorProperties;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

abstract class AbstractServiceBusContainerDockerCompose {
    private static final EmulatorProperties emulatorProperties = EmulatorProperties.get();

    private static final ComposeContainer environment = new ComposeContainer(
            new File(emulatorProperties.composerFile())
    )
            .withEnv(Map.of(
                    "ACCEPT_EULA", emulatorProperties.acceptEula(),
                    "CONFIG_FILE", emulatorProperties.configFile(),
                    "SQL_PASSWORD", emulatorProperties.sqlPassword()
            ))
            .withExposedService("emulator", 5672,
                    Wait.forListeningPort().withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS)))
    ;

    static {
        environment.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.azure.servicebus.connection-string",
                () -> String.format("Endpoint=sb://localhost:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;",
                        environment.getServicePort("emulator", 5672)));
        registry.add("emulator-ampq-port", () -> environment.getServicePort("emulator", 5672));
    }
}
