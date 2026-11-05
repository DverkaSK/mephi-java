package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;

import java.util.List;

public final class LogoutCommand implements Command {

    @Override
    public String name() {
        return "logout";
    }

    @Override
    public String usage() {
        return "logout";
    }

    @Override
    public String description() {
        return "Выйти: следующее сокращение создаст нового пользователя";
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 0, 0);
        context.session().logout();
        context.out().println("Вы вышли. Вернуться к своим ссылкам: login <UUID>");
        return CommandResult.CONTINUE;
    }
}
