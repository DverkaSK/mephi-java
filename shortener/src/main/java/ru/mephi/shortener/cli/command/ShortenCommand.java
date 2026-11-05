package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.domain.ShortLink;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public final class ShortenCommand implements Command {

    @Override
    public String name() {
        return "shorten";
    }

    @Override
    public List<String> aliases() {
        return List.of("s", "create");
    }

    @Override
    public String usage() {
        return "shorten <url> [лимит]";
    }

    @Override
    public String description() {
        return "Сократить ссылку, при необходимости задав лимит переходов";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "shorten https://www.baeldung.com/java-9-http-client",
                "shorten https://ru.stackoverflow.com 3",
                "shorten github.com/DverkaSK");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 1, 2);
        OptionalInt limit = args.size() == 2
                ? OptionalInt.of(Arguments.parseLimit(args.get(1)))
                : OptionalInt.empty();
        Optional<UUID> current = context.session().currentUser();
        UUID owner = current.orElseGet(context.session()::newUserId);

        ShortLink link = context.links().shorten(owner, args.getFirst(), limit);

        PrintStream out = context.out();
        if (current.isEmpty()) {
            context.session().login(owner);
            out.println("Создан новый пользователь. Ваш UUID: " + owner);
            out.println("Сохраните его: по нему можно вернуться к своим ссылкам командой login " + owner);
        }
        out.println("Ссылка создана: " + context.view().shortUrl(link));
        out.println(context.view().details(link));
        return CommandResult.CONTINUE;
    }
}
