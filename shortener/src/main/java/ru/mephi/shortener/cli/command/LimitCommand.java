package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.domain.ShortLink;

import java.util.List;

public final class LimitCommand implements Command {

    @Override
    public String name() {
        return "limit";
    }

    @Override
    public String usage() {
        return "limit <ссылка> <лимит>";
    }

    @Override
    public String description() {
        return "Изменить лимит переходов своей ссылки (разблокирует исчерпанную)";
    }

    @Override
    public List<String> examples() {
        return List.of("limit aB3xYz 50");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 2, 2);
        String code = Arguments.parseCode(context, args.getFirst());
        int limit = Arguments.parseLimit(args.get(1));
        ShortLink link = context.links().updateClickLimit(context.session().requireUser(), code, limit);
        context.out().printf("Лимит переходов для %s изменен: %d (использовано %d, осталось %d)%n",
                context.view().shortUrl(link), link.clickLimit(), link.clickCount(), link.remainingClicks());
        return CommandResult.CONTINUE;
    }
}
