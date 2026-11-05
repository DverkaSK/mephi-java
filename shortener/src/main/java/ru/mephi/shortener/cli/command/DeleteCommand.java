package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;

import java.util.List;

public final class DeleteCommand implements Command {

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public List<String> aliases() {
        return List.of("rm");
    }

    @Override
    public String usage() {
        return "delete <ссылка>";
    }

    @Override
    public String description() {
        return "Удалить свою ссылку";
    }

    @Override
    public List<String> examples() {
        return List.of("delete aB3xYz");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 1, 1);
        String code = Arguments.parseCode(context, args.getFirst());
        context.links().delete(context.session().requireUser(), code);
        context.out().println("Ссылка " + context.format().format(code) + " удалена");
        return CommandResult.CONTINUE;
    }
}
