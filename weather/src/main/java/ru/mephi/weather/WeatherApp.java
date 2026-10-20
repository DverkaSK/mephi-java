package ru.mephi.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public final class WeatherApp {

    static final String API_KEY_ENV = "YANDEX_WEATHER_KEY";

    static final int EXIT_OK = 0;
    static final int EXIT_FAILURE = 1;
    static final int EXIT_USAGE = 2;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final YandexWeatherClient client;
    private final ObjectMapper mapper;
    private final PrintStream out;

    WeatherApp(YandexWeatherClient client, ObjectMapper mapper, PrintStream out) {
        this.client = Objects.requireNonNull(client, "client");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.out = Objects.requireNonNull(out, "out");
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.getenv(), System.out, System.err);
        if (exitCode != EXIT_OK) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, Map<String, String> env, PrintStream out, PrintStream err) {
        try {
            WeatherRequest request = WeatherRequest.fromArgs(args);
            String apiKey = resolveApiKey(env);
            try (HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build()) {
                new WeatherApp(new YandexWeatherClient(httpClient, apiKey), new ObjectMapper(), out)
                        .printReport(request);
            }
            return EXIT_OK;
        } catch (ConfigurationException e) {
            err.println(e.getMessage());
            err.println("Использование: WeatherApp [lat] [lon] [limit]");
            return EXIT_USAGE;
        } catch (WeatherApiException e) {
            err.println(e.getMessage());
            err.println(e.getResponseBody());
            return EXIT_FAILURE;
        } catch (JsonProcessingException e) {
            err.println("Не удалось разобрать ответ сервиса: " + e.getOriginalMessage());
            return EXIT_FAILURE;
        } catch (IOException e) {
            err.println("Ошибка сетевого запроса: " + e);
            return EXIT_FAILURE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            err.println("Запрос был прерван");
            return EXIT_FAILURE;
        }
    }

    static String resolveApiKey(Map<String, String> env) {
        return Optional.ofNullable(env.get(API_KEY_ENV))
                .map(String::strip)
                .filter(Predicate.not(String::isEmpty))
                .orElseThrow(() -> new ConfigurationException(
                        "Не задан ключ API: установите переменную окружения " + API_KEY_ENV));
    }

    void printReport(WeatherRequest request) throws IOException, InterruptedException {
        ForecastResponse response = client.fetchForecast(request);

        out.println("GET " + response.requestUri());
        out.println("Полный ответ сервиса:");
        out.println(response.body());
        out.println();

        JsonNode forecast = mapper.readTree(response.body());

        out.println(ForecastAnalyzer.currentTemperature(forecast)
                .map(temp -> "Текущая температура (fact.temp): " + temp + " °C")
                .orElse("В ответе отсутствует fact.temp"));

        out.println(ForecastAnalyzer.averageDayTemperature(forecast, request.days())
                .map(avg -> String.format(Locale.ROOT,
                        "Средняя дневная температура (parts.day.temp_avg) за %d из %d запрошенных дн.: %.1f °C",
                        avg.days(), request.days(), avg.value()))
                .orElse("В ответе отсутствуют значения forecasts[*].parts.day.temp_avg"));
    }
}
