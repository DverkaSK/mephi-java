package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.domain.ShortLink;

import java.util.List;

public final class ListCommand implements Command {

    @Override
    public String name() {
        return "list";
    }

    @Override
    public List<String> aliases() {
        return List.of("ls");
    }

    @Override
    public String usage() {
        return "list";
    }

    @Override
    public String description() {
        return "Показать все ваши ссылки с переходами, статусом и сроком действия";
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 0, 0);
        List<ShortLink> links = context.links().linksOf(context.session().requireUser());
        if (links.isEmpty()) {
            context.out().println("У вас нет активных ссылок. Создать: shorten <url> [лимит]");
            return CommandResult.CONTINUE;
        }
        context.out().printf("Ваши ссылки (%d):%n", links.size());
        links.forEach(link -> context.out().println("  " + context.view().row(link)));
        return CommandResult.CONTINUE;
    }
}
