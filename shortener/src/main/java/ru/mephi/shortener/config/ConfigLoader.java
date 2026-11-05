package ru.mephi.shortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

public final class ConfigLoader {

    private static final String DEFAULT_RESOURCE = "/application.properties";

    private ConfigLoader() {
    }

    public static AppConfig load(Optional<Path> externalFile, Properties overrides) {
        Properties properties = readDefaults();
        externalFile.ifPresent(path -> properties.putAll(readFile(path)));
        ConfigKeys.ALL.stream()
                .filter(overrides::containsKey)
                .forEach(key -> properties.setProperty(key, overrides.getProperty(key)));
        return fromProperties(properties);
    }

    public static AppConfig fromProperties(Properties properties) {
        return new AppConfig(
                required(properties, ConfigKeys.DOMAIN),
                intValue(properties, ConfigKeys.CODE_LENGTH),
                durationValue(properties, ConfigKeys.LINK_TTL),
                intValue(properties, ConfigKeys.DEFAULT_CLICK_LIMIT),
                intValue(properties, ConfigKeys.MAX_CLICK_LIMIT),
                intValue(properties, ConfigKeys.MAX_URL_LENGTH),
                durationValue(properties, ConfigKeys.CLEANUP_INTERVAL),
                storagePath(properties.getProperty(ConfigKeys.STORAGE_FILE, "")));
    }

    private static Properties readDefaults() {
        try (InputStream in = ConfigLoader.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new ConfigurationException("Не найден ресурс " + DEFAULT_RESOURCE);
            }
            return read(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ConfigurationException("Не удалось прочитать " + DEFAULT_RESOURCE, e);
        }
    }

    private static Properties readFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new ConfigurationException("Файл конфигурации не найден: " + path.toAbsolutePath());
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return read(reader);
        } catch (IOException e) {
            throw new ConfigurationException("Не удалось прочитать файл конфигурации " + path, e);
        }
    }

    private static Properties read(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Не задан параметр конфигурации " + key);
        }
        return value.strip();
    }

    private static int intValue(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(
                    "Параметр %s должен быть целым числом, получено '%s'".formatted(key, value), e);
        }
    }

    private static Duration durationValue(Properties properties, String key) {
        return DurationParser.parse(key, required(properties, key));
    }

    private static Optional<Path> storagePath(String value) {
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        String expanded = trimmed.startsWith("~")
                ? System.getProperty("user.home") + trimmed.substring(1)
                : trimmed;
        try {
            return Optional.of(Path.of(expanded));
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Некорректный путь в %s: %s".formatted(ConfigKeys.STORAGE_FILE, value), e);
        }
    }
}
