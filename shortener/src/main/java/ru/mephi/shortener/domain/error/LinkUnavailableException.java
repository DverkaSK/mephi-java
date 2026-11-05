package ru.mephi.shortener.domain.error;

import ru.mephi.shortener.domain.LinkStatus;
import ru.mephi.shortener.domain.ShortLink;

import java.io.Serial;
import java.util.Objects;
import java.util.UUID;

public final class LinkUnavailableException extends ShortenerException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final UUID owner;
    private final LinkStatus reason;

    public LinkUnavailableException(ShortLink link, LinkStatus reason) {
        super(describe(link.code(), reason));
        if (reason == LinkStatus.ACTIVE) {
            throw new IllegalArgumentException("Active link cannot be unavailable");
        }
        this.code = link.code();
        this.owner = link.owner();
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public String code() {
        return code;
    }

    public UUID owner() {
        return owner;
    }

    public LinkStatus reason() {
        return reason;
    }

    private static String describe(String code, LinkStatus reason) {
        return reason == LinkStatus.EXPIRED
                ? "Срок жизни ссылки %s истек".formatted(code)
                : "Лимит переходов по ссылке %s исчерпан".formatted(code);
    }
}
