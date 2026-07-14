package config;

import util.FilesUtil;

import java.util.Properties;

public record ApplicationConfig(String url) {

    public static ApplicationConfig load() {
        try {
            Properties properties = new Properties();
            properties.load(new java.io.StringReader(FilesUtil.read("application.properties")));
            String url = properties.getProperty("application.url", "").trim();
            if (url.isBlank()) {
                throw new RuntimeException("Не задано свойство application.url");
            }
            return new ApplicationConfig(url);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить application.properties", e);
        }
    }
}
