package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;

import java.util.List;
import java.util.UUID;

public final class LoginCommand implements Command {

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String usage() {
        return "login <UUID>";
    }

    @Override
    public String description() {
        return "Переключиться на пользователя с указанным UUID";
    }

    @Override
    public List<String> examples() {
        return List.of("login 3f2b8c1e-5d4a-4e6b-9c7d-1a2b3c4d5e6f");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 1, 1);
        UUID user = Arguments.parseUuid(args.getFirst());
        context.session().login(user);
        int count = context.links().linksOf(user).size();
        context.out().println("Вы вошли как " + user);
        context.out().println(count == 0
                ? "У этого пользователя пока нет активных ссылок"
                : "Активных ссылок: " + count + ". Посмотреть: list");
        return CommandResult.CONTINUE;
    }
}
