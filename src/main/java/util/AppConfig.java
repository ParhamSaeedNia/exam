package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Config file not found: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getAppName() {
        return getProperty("app.name", "Bank Account Management System");
    }

    public static String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:h2:./bankdb");
    }

    public static String getDbUser() {
        return getProperty("db.user", "sa");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "");
    }

    public static String getMessage(String key) {
        return getProperty("msg." + key, key);
    }
}
