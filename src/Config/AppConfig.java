package Config;

public class AppConfig {

    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;

    public AppConfig(String databaseUrl, String databaseUsername, String databasePassword) {
        this.databaseUrl = requireValue(databaseUrl, "db.url");
        this.databaseUsername = requireValue(databaseUsername, "db.username");
        this.databasePassword = databasePassword == null ? "" : databasePassword;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    private String requireValue(String value, String propertyName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required configuration property: " + propertyName);
        }
        return value.trim();
    }
}
