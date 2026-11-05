package ru.mephi.weather;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public final class YandexWeatherClient {

    public static final URI DEFAULT_ENDPOINT = URI.create("https://api.weather.yandex.ru/v2/forecast");

    public static final String API_KEY_HEADER = "X-Yandex-Weather-Key";

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);
    private static final int HTTP_OK = 200;

    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;

    public YandexWeatherClient(HttpClient httpClient, String apiKey) {
        this(httpClient, DEFAULT_ENDPOINT, apiKey);
    }

    public YandexWeatherClient(HttpClient httpClient, URI endpoint, String apiKey) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
    }

    public URI forecastUri(WeatherRequest request) {
        return URI.create("%s?lat=%s&lon=%s&limit=%d".formatted(
                endpoint, toPlainString(request.latitude()), toPlainString(request.longitude()), request.days()));
    }

    public ForecastResponse fetchForecast(WeatherRequest request) throws IOException, InterruptedException {
        URI uri = forecastUri(request);
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .header(API_KEY_HEADER, apiKey)
                .header("Accept", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HTTP_OK) {
            throw new WeatherApiException(response.statusCode(), response.body());
        }
        return new ForecastResponse(uri, response.body());
    }

    private static String toPlainString(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
