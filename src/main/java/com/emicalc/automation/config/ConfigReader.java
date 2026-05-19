package com.emicalc.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final String FILE = "config.properties";
    private static ConfigReader instance;
    private final Properties props = new Properties();

    private ConfigReader() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(FILE)) {
            if (is == null) throw new IllegalStateException(FILE + " not found on classpath");
            props.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + FILE, e);
        }
    }

    public static synchronized ConfigReader get() {
        if (instance == null) instance = new ConfigReader();
        return instance;
    }

    public String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();
        String val = props.getProperty(key);
        if (val == null) throw new IllegalArgumentException("Missing config key: " + key);
        return val.trim();
    }

    public String get(String key, String def) {
        try { return get(key); } catch (IllegalArgumentException e) { return def; }
    }

    public int     getInt(String key)     { return Integer.parseInt(get(key)); }
    public double  getDouble(String key)  { return Double.parseDouble(get(key)); }
    public boolean getBoolean(String key) { return Boolean.parseBoolean(get(key)); }
}
