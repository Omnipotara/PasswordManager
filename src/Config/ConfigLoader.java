package Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE_NAME = "application.properties";

    private ConfigLoader() {
    }

    public static AppConfig load() {
        Path configPath = Paths.get(CONFIG_FILE_NAME);
        if (!Files.exists(configPath)) {
            throw new IllegalStateException(
                    "Missing " + CONFIG_FILE_NAME + ". Copy application.example.properties and set local values.");
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + CONFIG_FILE_NAME + ".", ex);
        }

        return new AppConfig(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password"),
                properties.getProperty("smtp.host"),
                properties.getProperty("smtp.port"),
                properties.getProperty("smtp.username"),
                properties.getProperty("smtp.password"),
                properties.getProperty("smtp.auth"),
                properties.getProperty("smtp.starttls"));
    }
}
