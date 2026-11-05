package ru.mephi.shortener.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigLoaderTest {

    @Test
    public void loadsBundledDefaults() {
        AppConfig config = ConfigLoader.load(Optional.empty(), new Properties());

        assertEquals("clck.ru", config.domain());
        assertEquals(6, config.codeLength());
        assertEquals(Duration.ofHours(24), config.linkTtl());
        assertEquals(10, config.defaultClickLimit());
        assertEquals(100_000, config.maxClickLimit());
        assertEquals(Duration.ofSeconds(30), config.cleanupInterval());
        assertTrue(config.storageFile().orElseThrow().endsWith(Path.of(".shortener", "links.json")));
    }

    @Test
    public void externalFileAndOverridesTakePrecedence(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("custom.properties");
        Files.writeString(file, "link.ttl=1h\nlink.click-limit.default=3\nstorage.file=\n");
        Properties overrides = new Properties();
        overrides.setProperty("link.code-length", "8");
        overrides.setProperty("unrelated.key", "ignored");

        AppConfig config = ConfigLoader.load(Optional.of(file), overrides);

        assertEquals(Duration.ofHours(1), config.linkTtl());
        assertEquals(3, config.defaultClickLimit());
        assertEquals(8, config.codeLength());
        assertEquals(Optional.empty(), config.storageFile());
    }

    @ParameterizedTest
    @CsvSource({"30s, PT30S", "15m, PT15M", "2h, PT2H", "1d, PT24H", "PT1H30M, PT1H30M", "p1d, PT24H"})
    public void parsesDurations(String text, String expected) {
        assertEquals(Duration.parse(expected), DurationParser.parse("key", text));
    }

    @ParameterizedTest
    @CsvSource({
            "link.ttl, soon",
            "link.ttl, 0s",
            "link.code-length, 2",
            "link.code-length, six",
            "link.click-limit.default, 0",
            "link.click-limit.max, 5",
            "link.domain, https://clck.ru/",
            "cleanup.interval, -1"
    })
    public void rejectsInvalidValues(String key, String value) {
        Properties overrides = new Properties();
        overrides.setProperty(key, value);

        assertThrows(ConfigurationException.class, () -> ConfigLoader.load(Optional.empty(), overrides));
    }

    @Test
    public void reportsMissingFile(@TempDir Path dir) {
        ConfigurationException error = assertThrows(ConfigurationException.class,
                () -> ConfigLoader.load(Optional.of(dir.resolve("absent.properties")), new Properties()));
        assertTrue(error.getMessage().contains("absent.properties"));
    }
}
