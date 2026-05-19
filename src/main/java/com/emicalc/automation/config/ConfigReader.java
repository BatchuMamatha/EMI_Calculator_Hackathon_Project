package com.emicalc.automation.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton properties loader. Reads src/main/resources/config.properties
 * and exposes typed getters. System properties (-D...) take precedence so
 * Jenkins / CLI can override anything (e.g. -Dbrowser=edge -Dheadless=true).
 */
public final class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";
    private static ConfigReader instance;
    private final Properties props = new Properties();

    private ConfigReader() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found on classpath");
            }
            props.load(is);
            log.info("Loaded {} properties from {}", props.size(), CONFIG_FILE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    public static synchronized ConfigReader get() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    public String get(String key) {
        // -D system prop wins over file
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();
        String val = props.getProperty(key);
        if (val == null) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return val.trim();
    }

    public String get(String key, String defaultVal) {
        try {
            return get(key);
        } catch (IllegalArgumentException e) {
            return defaultVal;
        }
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
