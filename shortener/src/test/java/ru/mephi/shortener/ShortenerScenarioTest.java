package ru.mephi.shortener;

import org.junit.jupiter.api.Test;
import ru.mephi.shortener.cli.CommandResult;
import ru.mephi.shortener.config.AppConfig;
import ru.mephi.shortener.config.ConfigLoader;
import ru.mephi.shortener.infra.storage.InMemoryLinkRepository;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShortenerScenarioTest {

    private static final String URL = "https://www.baeldung.com/java-9-http-client";
    private static final UUID ALICE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BOB = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Pattern SHORT_LINK = Pattern.compile("Ссылка создана: (clck\\.ru/(\\w+))");

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-05T10:00:00Z"));
    private final List<URI> opened = new ArrayList<>();
    private final Deque<UUID> userIds = new ArrayDeque<>(List.of(ALICE, BOB));
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ShortenerApp.Components app = ShortenerApp.assemble(
            config(), new InMemoryLinkRepository(), uri -> opened.add(uri), clock, userIds::poll,
            new PrintStream(buffer, true, StandardCharsets.UTF_8));

    private static AppConfig config() {
        Properties overrides = new Properties();
        overrides.setProperty("storage.file", "");
        overrides.setProperty("link.click-limit.default", "5");
        return ConfigLoader.load(Optional.empty(), overrides);
    }

    private String exec(String line) {
        buffer.reset();
        app.cli().handle(line);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String code(String output) {
        Matcher matcher = SHORT_LINK.matcher(output);
        assertTrue(matcher.find(), output);
        return matcher.group(2);
    }

    @Test
    public void firstShortenCreatesUserAndPastedLinkOpensBrowser() {
        String created = exec("shorten " + URL + " 2");
        String code = code(created);

        assertTrue(created.contains("Ваш UUID: " + ALICE), created);
        assertTrue(created.contains("0 из 2"), created);

        String followed = exec("clck.ru/" + code);
        assertEquals(List.of(URI.create(URL)), opened);
        assertTrue(followed.contains("Осталось переходов: 1 из 2"), followed);
    }

    @Test
    public void differentUsersGetDifferentLinksForSameUrl() {
        String alices = code(exec("shorten " + URL));
        exec("logout");
        String bobs = code(exec("shorten " + URL));

        assertNotEquals(alices, bobs);
        assertTrue(exec("whoami").contains(BOB.toString()));
        assertTrue(exec("list").contains("clck.ru/" + bobs));
        assertFalse(exec("list").contains(alices));
    }

    @Test
    public void limitExhaustionBlocksLinkAndNotifiesOwner() throws IOException {
        String code = code(exec("shorten " + URL + " 1"));
        exec("open " + code);

        String blocked = exec("open " + code);
        assertTrue(blocked.contains("лимит переходов исчерпан"), blocked);
        assertTrue(blocked.contains("limit " + code), blocked);
        assertEquals(1, opened.size());

        buffer.reset();
        app.cli().run(new BufferedReader(new StringReader("exit\n")));
        String session = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(session.contains("[уведомление] Лимит переходов по ссылке clck.ru/" + code), session);

        assertTrue(exec("limit " + code + " 3").contains("осталось 2"));
        exec("open " + code);
        assertEquals(2, opened.size());
    }

    @Test
    public void expiredLinkIsRemovedAndOwnerIsNotified() throws IOException {
        String code = code(exec("shorten " + URL));
        clock.advance(Duration.ofHours(23));
        assertTrue(exec("info " + code).contains("осталось 1 ч"));

        clock.advance(Duration.ofHours(1));
        app.links().purgeExpired();

        String reopened = exec("clck.ru/" + code);
        assertTrue(reopened.contains("не найдена"), reopened);
        assertTrue(exec("list").contains("нет активных ссылок"));

        buffer.reset();
        app.cli().run(new BufferedReader(new StringReader("")));
        String session = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(session.contains("Срок жизни ссылки clck.ru/" + code), session);
        assertTrue(opened.isEmpty());
    }

    @Test
    public void anotherUserCannotManageForeignLink() {
        String code = code(exec("shorten " + URL));
        exec("logout");
        exec("shorten https://openjdk.org");

        assertTrue(exec("delete " + code).contains("принадлежит другому пользователю"));
        assertTrue(exec("limit " + code + " 100").contains("принадлежит другому пользователю"));
        assertTrue(exec("edit " + code + " https://example.com").contains("принадлежит другому пользователю"));

        exec("login " + ALICE);
        assertTrue(exec("delete clck.ru/" + code).contains("удалена"));
    }

    @Test
    public void invalidInputProducesFriendlyErrors() {
        assertTrue(exec("shorten").contains("Использование: shorten <url> [лимит]"));
        assertTrue(exec("shorten ftp://files.example.com").contains("не поддерживается"));
        assertTrue(exec("shorten " + URL + " many").contains("целым числом"));
        assertTrue(exec("shorten " + URL + " 0").contains("от 1 до"));
        assertTrue(exec("list").contains("Ошибка"), "list before login");
        assertTrue(exec("login not-a-uuid").contains("не является UUID"));
        assertTrue(exec("open ???").contains("не похоже на короткую ссылку"));
        assertTrue(exec("frobnicate").contains("Неизвестная команда"));
        assertEquals("", exec("   "));
    }

    @Test
    public void helpListsCommandsAndExitStopsLoop() {
        String help = exec("help");
        List.of("shorten", "open", "list", "info", "limit", "edit", "delete", "whoami", "login", "logout", "exit")
                .forEach(name -> assertTrue(help.contains(name), name));
        assertTrue(exec("help limit").contains("limit aB3xYz 50"));
        assertEquals(CommandResult.EXIT, app.cli().handle("exit"));
    }
}
