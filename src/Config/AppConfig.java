package Config;

public class AppConfig {

    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final boolean smtpAuth;
    private final boolean smtpStartTls;

    public AppConfig(
            String databaseUrl,
            String databaseUsername,
            String databasePassword,
            String smtpHost,
            String smtpPort,
            String smtpUsername,
            String smtpPassword,
            String smtpAuth,
            String smtpStartTls) {
        this.databaseUrl = requireValue(databaseUrl, "db.url");
        this.databaseUsername = requireValue(databaseUsername, "db.username");
        this.databasePassword = databasePassword == null ? "" : databasePassword;
        this.smtpHost = requireValue(smtpHost, "smtp.host");
        this.smtpPort = parsePort(smtpPort);
        this.smtpUsername = requireValue(smtpUsername, "smtp.username");
        this.smtpPassword = requireValue(smtpPassword, "smtp.password");
        this.smtpAuth = parseBoolean(smtpAuth, "smtp.auth");
        this.smtpStartTls = parseBoolean(smtpStartTls, "smtp.starttls");
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

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public boolean isSmtpStartTls() {
        return smtpStartTls;
    }

    private String requireValue(String value, String propertyName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required configuration property: " + propertyName);
        }
        return value.trim();
    }

    private int parsePort(String value) {
        String portValue = requireValue(value, "smtp.port");
        try {
            int port = Integer.parseInt(portValue);
            if (port <= 0 || port > 65535) {
                throw new IllegalStateException("Invalid SMTP port: " + portValue);
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid SMTP port: " + portValue, ex);
        }
    }

    private boolean parseBoolean(String value, String propertyName) {
        String booleanValue = requireValue(value, propertyName);
        if ("true".equalsIgnoreCase(booleanValue)) {
            return true;
        }
        if ("false".equalsIgnoreCase(booleanValue)) {
            return false;
        }
        throw new IllegalStateException("Invalid boolean configuration property: " + propertyName);
    }
}
