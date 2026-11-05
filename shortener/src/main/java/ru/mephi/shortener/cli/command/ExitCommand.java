package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;

import java.util.List;

public final class ExitCommand implements Command {

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public List<String> aliases() {
        return List.of("quit", "q");
    }

    @Override
    public String usage() {
        return "exit";
    }

    @Override
    public String description() {
        return "Выйти из программы";
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 0, 0);
        context.session().currentUser()
                .ifPresent(user -> context.out().println("Ваш UUID для следующего входа: " + user));
        context.out().println("До свидания!");
        return CommandResult.EXIT;
    }
}
