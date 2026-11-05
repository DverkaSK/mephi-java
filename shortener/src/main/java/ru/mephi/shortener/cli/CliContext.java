package ru.mephi.shortener.cli;

import ru.mephi.shortener.service.LinkService;
import ru.mephi.shortener.service.ShortLinkFormat;
import ru.mephi.shortener.service.port.NotificationInbox;

import java.io.PrintStream;
import java.util.Objects;

public record CliContext(
        LinkService links,
        Session session,
        NotificationInbox inbox,
        BrowserLauncher browser,
        ShortLinkFormat format,
        LinkView view,
        PrintStream out) {

    public CliContext {
        Objects.requireNonNull(links, "links");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(inbox, "inbox");
        Objects.requireNonNull(browser, "browser");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(out, "out");
    }
}
