package example.testcontainers.servicebusemulator.code;

import example.testcontainers.servicebusemulator.EmulatorProperties;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

abstract class AbstractServiceBusContainerCodeCompose {

    private static final EmulatorProperties emulatorProperties = EmulatorProperties.get();

    private static final Network network = Network.newNetwork();

    private static final GenericContainer<?> sqledge = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-sql-edge:latest"))
            .withNetwork(network)
            .withNetworkAliases("sqledge")
            .withEnv("ACCEPT_EULA", emulatorProperties.acceptEula())
            .withEnv("MSSQL_SA_PASSWORD", emulatorProperties.sqlPassword());

    private static final GenericContainer<?> emulator = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-messaging/servicebus-emulator:latest"))
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withNetwork(network)
            .withEnv("ACCEPT_EULA", emulatorProperties.acceptEula())
            .withEnv("SQL_SERVER", "sqledge")
            .withEnv("MSSQL_SA_PASSWORD", emulatorProperties.sqlPassword())
            .dependsOn(sqledge)
            .withExposedPorts(5672)
            .withFileSystemBind(emulatorProperties.configFile(), "/ServiceBus_Emulator/ConfigFiles/Config.json", BindMode.READ_ONLY);

    static {
        sqledge.start();
        emulator.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.azure.servicebus.connection-string",
                () -> String.format("Endpoint=sb://localhost:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;",
                        emulator.getFirstMappedPort()));
        registry.add("emulator-ampq-port", emulator::getFirstMappedPort);
    }
}
