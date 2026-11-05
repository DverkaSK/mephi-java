package ru.mephi.shortener.domain.error;

import java.io.Serial;

public final class LinkNotFoundException extends ShortenerException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LinkNotFoundException(String code) {
        super("Ссылка с кодом %s не найдена".formatted(code));
    }
}
