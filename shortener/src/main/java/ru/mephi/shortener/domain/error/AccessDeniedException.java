package ru.mephi.shortener.domain.error;

import java.io.Serial;

public final class AccessDeniedException extends ShortenerException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String code) {
        super("Ссылка с кодом %s принадлежит другому пользователю, управлять ею может только владелец".formatted(code));
    }
}
