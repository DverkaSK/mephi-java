package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;

import java.util.List;

public final class WhoAmICommand implements Command {

    @Override
    public String name() {
        return "whoami";
    }

    @Override
    public String usage() {
        return "whoami";
    }

    @Override
    public String description() {
        return "Показать ваш UUID";
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 0, 0);
        context.out().println(context.session().currentUser()
                .map(user -> "Ваш UUID: " + user)
                .orElse("UUID еще не выдан: он будет создан при первом сокращении ссылки"));
        return CommandResult.CONTINUE;
    }
}
