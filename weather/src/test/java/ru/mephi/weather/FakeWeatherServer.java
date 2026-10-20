package ru.mephi.weather;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

final class FakeWeatherServer implements AutoCloseable {

    private static final String PATH = "/v2/forecast";

    private final HttpServer server;
    private final AtomicReference<RecordedRequest> lastRequest = new AtomicReference<>();

    private FakeWeatherServer(HttpServer server) {
        this.server = server;
    }

    static FakeWeatherServer respondingWith(int status, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        FakeWeatherServer fake = new FakeWeatherServer(server);
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);

        server.createContext(PATH, exchange -> {
            fake.lastRequest.set(new RecordedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getRawQuery(),
                    exchange.getRequestHeaders().getFirst(YandexWeatherClient.API_KEY_HEADER)));
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        });
        server.start();
        return fake;
    }

    URI endpoint() {
        return URI.create("http://%s:%d%s".formatted(
                server.getAddress().getHostString(), server.getAddress().getPort(), PATH));
    }

    Optional<RecordedRequest> lastRequest() {
        return Optional.ofNullable(lastRequest.get());
    }

    @Override
    public void close() {
        server.stop(0);
    }

    record RecordedRequest(String method, String query, String apiKey) {
    }
}
