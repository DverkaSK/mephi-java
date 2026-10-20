package ru.mephi.weather;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YandexWeatherClientTest {

    private static final String API_KEY = "test-key";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        httpClient.close();
    }

    @Test
    void buildsForecastUriWithPlainNumbers() {
        YandexWeatherClient client = new YandexWeatherClient(httpClient, API_KEY);

        assertEquals("https://api.weather.yandex.ru/v2/forecast?lat=55.75&lon=37.62&limit=3",
                client.forecastUri(new WeatherRequest(55.75, 37.62, 3)).toString());
        assertEquals("https://api.weather.yandex.ru/v2/forecast?lat=0.00001&lon=-37&limit=1",
                client.forecastUri(new WeatherRequest(1e-5, -37.0, 1)).toString());
    }

    @Test
    void sendsGetWithApiKeyHeaderAndLimit() throws Exception {
        try (FakeWeatherServer server = FakeWeatherServer.respondingWith(200, TestData.FORECAST_RESPONSE)) {
            YandexWeatherClient client = new YandexWeatherClient(httpClient, server.endpoint(), API_KEY);

            ForecastResponse response = client.fetchForecast(new WeatherRequest(55.75, 37.62, 3));

            FakeWeatherServer.RecordedRequest recorded = server.lastRequest().orElseThrow();
            assertEquals("GET", recorded.method());
            assertEquals("lat=55.75&lon=37.62&limit=3", recorded.query());
            assertEquals(API_KEY, recorded.apiKey());
            assertEquals(TestData.FORECAST_RESPONSE, response.body());
        }
    }

    @Test
    void throwsOnNonSuccessStatus() throws Exception {
        try (FakeWeatherServer server = FakeWeatherServer.respondingWith(403, "{\"message\":\"forbidden\"}")) {
            YandexWeatherClient client = new YandexWeatherClient(httpClient, server.endpoint(), API_KEY);

            WeatherApiException e = assertThrows(WeatherApiException.class,
                    () -> client.fetchForecast(new WeatherRequest(55.75, 37.62, 3)));

            assertEquals(403, e.getStatusCode());
            assertEquals("{\"message\":\"forbidden\"}", e.getResponseBody());
        }
    }
}
