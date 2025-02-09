package example.testcontainers.servicebusemulator;


import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class EmulatorProperties {
    public static final String COMPOSER_FILE = "COMPOSER_FILE";
    public static final String ACCEPT_EULA = "ACCEPT_EULA";
    public static final String CONFIG_FILE = "CONFIG_FILE";
    private static final String SQL_PASSWORD = "SQL_PASSWORD";
    private final Properties properties;

    private EmulatorProperties(Properties properties) {
        this.properties = properties;
    }

    public String composerFile() {
        return properties.getProperty(COMPOSER_FILE);
    }

    public String acceptEula() {
        return properties.getProperty(ACCEPT_EULA);
    }

    public String configFile() {
        return properties.getProperty(CONFIG_FILE);
    }

    public String sqlPassword() {
        return properties.getProperty(SQL_PASSWORD);
    }

    public static EmulatorProperties get() {
        return new EmulatorProperties(getProperties());
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = EmulatorProperties.class.getClassLoader().getResourceAsStream("servicebus-emulator.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // validate properties
        if (!"Y".equalsIgnoreCase(properties.getProperty(ACCEPT_EULA)) && "Y".equalsIgnoreCase(getEnvironmentProperty(ACCEPT_EULA))) {
            properties.setProperty(ACCEPT_EULA, getEnvironmentProperty(ACCEPT_EULA));
        }
        if (!StringUtils.hasText(properties.getProperty(SQL_PASSWORD)) && getEnvironmentProperty(SQL_PASSWORD) != null) {
            properties.setProperty(SQL_PASSWORD, getEnvironmentProperty(SQL_PASSWORD));
        }
        if (!"Y".equalsIgnoreCase(properties.getProperty(ACCEPT_EULA))) {
            throw new RuntimeException("You must accept the EULA by setting a property in the servicebus-emulator.properties file or by setting environment variable ACCEPT_EULA");
        }
        if (!StringUtils.hasText(properties.getProperty(SQL_PASSWORD))) {
            throw new RuntimeException("You must set SQL Edge password - https://learn.microsoft.com/en-us/sql/relational-databases/security/strong-passwords?view=sql-server-linux-ver16");
        }
        if (!properties.containsKey(CONFIG_FILE) || Files.notExists(Path.of(properties.getProperty(CONFIG_FILE)))) {
            throw new RuntimeException("The emulator configuration file does not exist.");
        }

        try {
            String configFile = properties.getProperty(CONFIG_FILE);
            String configFileCanonicalPath = Path.of(configFile).toFile().getCanonicalPath();
            if (!configFile.equals(configFileCanonicalPath)) {
                properties.setProperty(CONFIG_FILE, configFileCanonicalPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error resolving configuration file canonical path", e);
        }

        return properties;
    }

    private static String getEnvironmentProperty(String key) {
        if (StringUtils.hasText(System.getProperty(key))) {
            return System.getProperty(key);
        }
        if (StringUtils.hasText(System.getenv(key))) {
            return System.getenv(key);
        }
        return null;
    }
}
