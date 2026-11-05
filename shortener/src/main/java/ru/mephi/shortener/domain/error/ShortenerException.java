package ru.mephi.shortener.domain.error;

import java.io.Serial;

public abstract sealed class ShortenerException extends RuntimeException
        permits AccessDeniedException, CodeGenerationException, InvalidInputException,
        LinkNotFoundException, LinkUnavailableException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ShortenerException(String message) {
        super(message);
    }
}
