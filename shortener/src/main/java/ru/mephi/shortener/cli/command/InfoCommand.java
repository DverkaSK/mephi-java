package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.domain.ShortLink;

import java.util.List;

public final class InfoCommand implements Command {

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String usage() {
        return "info <ссылка>";
    }

    @Override
    public String description() {
        return "Подробная информация о вашей ссылке";
    }

    @Override
    public List<String> examples() {
        return List.of("info aB3xYz");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 1, 1);
        String code = Arguments.parseCode(context, args.getFirst());
        ShortLink link = context.links().linkOf(context.session().requireUser(), code);
        context.out().println(context.view().details(link));
        return CommandResult.CONTINUE;
    }
}
