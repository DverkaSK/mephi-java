package ru.mephi.shortener.domain.error;

import java.io.Serial;

public final class CodeGenerationException extends ShortenerException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CodeGenerationException(int attempts) {
        super("Не удалось подобрать свободный короткий код за %d попыток".formatted(attempts));
    }
}
