package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.domain.ShortLink;

import java.util.List;

public final class EditCommand implements Command {

    @Override
    public String name() {
        return "edit";
    }

    @Override
    public String usage() {
        return "edit <ссылка> <новый url>";
    }

    @Override
    public String description() {
        return "Изменить адрес, на который ведет ваша ссылка";
    }

    @Override
    public List<String> examples() {
        return List.of("edit aB3xYz https://openjdk.org");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 2, 2);
        String code = Arguments.parseCode(context, args.getFirst());
        ShortLink link = context.links().updateUrl(context.session().requireUser(), code, args.get(1));
        context.out().printf("Ссылка %s теперь ведет на %s%n", context.view().shortUrl(link), link.originalUrl());
        return CommandResult.CONTINUE;
    }
}
