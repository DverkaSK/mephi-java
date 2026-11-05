package ru.mephi.shortener.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mephi.shortener.domain.error.InvalidInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator(64, "clck.ru");

    @ParameterizedTest
    @CsvSource({
            "https://www.baeldung.com/java-9-http-client, https://www.baeldung.com/java-9-http-client",
            "'  http://example.com/a?b=c  ', http://example.com/a?b=c",
            "ru.stackoverflow.com, https://ru.stackoverflow.com",
            "HTTPS://Example.com, HTTPS://Example.com",
            "http://localhost:8080/health, http://localhost:8080/health"
    })
    public void acceptsAndNormalizesValidUrls(String input, String expected) {
        assertEquals(expected, validator.normalize(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    public void rejectsBlankInput(String input) {
        assertThrows(InvalidInputException.class, () -> validator.normalize(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://files.example.com",
            "javascript://alert(1)",
            "https://",
            "https://exa mple.com",
            "https://nodot",
            "https://host:99999999",
            "http://[broken"
    })
    public void rejectsMalformedOrUnsupportedUrls(String input) {
        assertThrows(InvalidInputException.class, () -> validator.normalize(input));
    }

    @Test
    public void rejectsTooLongUrl() {
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> validator.normalize("https://example.com/" + "a".repeat(64)));
        assertTrue(error.getMessage().contains("64"));
    }

    @Test
    public void rejectsAlreadyShortenedLink() {
        assertThrows(InvalidInputException.class, () -> validator.normalize("clck.ru/aB3xYz"));
    }
}
