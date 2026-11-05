package ru.mephi.shortener.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShortLinkFormatTest {

    private final ShortLinkFormat format = new ShortLinkFormat("clck.ru");

    @Test
    public void formatsCodeWithDomain() {
        assertEquals("clck.ru/3DZHeG", format.format("3DZHeG"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "3DZHeG", "clck.ru/3DZHeG", "https://clck.ru/3DZHeG", "HTTP://CLCK.RU/3DZHeG", " clck.ru/3DZHeG "})
    public void parsesCodeFromAnySupportedForm(String input) {
        assertEquals(Optional.of("3DZHeG"), format.parseCode(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "clck.ru/", "clck.ru/abc/def", "other.ru/3DZHeG", "abc-def", "clck.ru/ab cd"})
    public void rejectsInvalidInput(String input) {
        assertEquals(Optional.empty(), format.parseCode(input));
    }

    @Test
    public void recognizesOnlyFullShortLinks() {
        assertTrue(format.isShortLink("clck.ru/3DZHeG"));
        assertTrue(format.isShortLink("https://clck.ru/3DZHeG"));
        assertFalse(format.isShortLink("3DZHeG"));
        assertFalse(format.isShortLink("list"));
    }
}
