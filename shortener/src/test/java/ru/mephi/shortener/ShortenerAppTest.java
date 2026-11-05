package ru.mephi.shortener;

import org.junit.jupiter.api.Test;
import ru.mephi.shortener.config.ConfigurationException;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShortenerAppTest {

    @Test
    void parsesConfigOptionInBothForms() {
        assertEquals(Optional.of(Path.of("a.properties")),
                ShortenerApp.configPath(new String[] {"--config", "a.properties"}));
        assertEquals(Optional.of(Path.of("b.properties")),
                ShortenerApp.configPath(new String[] {"--config=b.properties"}));
    }

    @Test
    void rejectsUnknownArguments() {
        assertThrows(ConfigurationException.class, () -> ShortenerApp.configPath(new String[] {"--verbose"}));
    }
}
