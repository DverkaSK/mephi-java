package ru.mephi.shortener.domain.error;

import java.io.Serial;

public final class InvalidInputException extends ShortenerException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
