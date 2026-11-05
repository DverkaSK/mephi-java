package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.domain.error.LinkUnavailableException;

import java.io.PrintStream;
import java.net.URI;
import java.util.List;

public final class OpenCommand implements Command {

    public static final String NAME = "open";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public List<String> aliases() {
        return List.of("go");
    }

    @Override
    public String usage() {
        return "open <ссылка>";
    }

    @Override
    public String description() {
        return "Перейти по короткой ссылке в браузере (можно просто вставить ссылку)";
    }

    @Override
    public List<String> examples() {
        return List.of("open clck.ru/aB3xYz", "open aB3xYz", "clck.ru/aB3xYz");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 1, 1);
        String code = Arguments.parseCode(context, args.getFirst());
        try {
            redirect(context.links().follow(code), context);
        } catch (LinkUnavailableException e) {
            boolean ownerIsViewing = context.session().currentUser().filter(e.owner()::equals).isPresent();
            context.out().println(context.view().unavailable(e, ownerIsViewing));
        }
        return CommandResult.CONTINUE;
    }

    private static void redirect(ShortLink link, CliContext context) {
        PrintStream out = context.out();
        out.println("Переход: " + context.view().shortUrl(link) + " -> " + link.originalUrl());
        if (!context.browser().open(URI.create(link.originalUrl()))) {
            out.println("Не удалось открыть браузер, откройте адрес вручную: " + link.originalUrl());
        }
        if (link.isLimitExhausted()) {
            out.println("Это был последний разрешенный переход, теперь ссылка заблокирована");
        } else {
            out.printf("Осталось переходов: %d из %d%n", link.remainingClicks(), link.clickLimit());
        }
    }
}
