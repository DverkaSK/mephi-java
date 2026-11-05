package ru.mephi.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeatherAppTest {

    private final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBuffer, true, StandardCharsets.UTF_8);

    @Test
    public void printsFullResponseCurrentAndAverageTemperature() throws Exception {
        try (FakeWeatherServer server = FakeWeatherServer.respondingWith(200, TestData.FORECAST_RESPONSE);
             HttpClient httpClient = HttpClient.newHttpClient()) {
            YandexWeatherClient client = new YandexWeatherClient(httpClient, server.endpoint(), "key");

            new WeatherApp(client, new ObjectMapper(), out).printReport(new WeatherRequest(55.75, 37.62, 2));
        }

        String output = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains(TestData.FORECAST_RESPONSE), output);
        assertTrue(output.contains("Текущая температура (fact.temp): 7 °C"), output);
        assertTrue(output.contains("за 2 из 2 запрошенных дн.: 7.0 °C"), output);
    }

    @Test
    public void failsWithUsageCodeWhenApiKeyMissing() {
        int exitCode = WeatherApp.run(new String[0], Map.of(), out, err);

        assertEquals(WeatherApp.EXIT_USAGE, exitCode);
        assertTrue(errBuffer.toString(StandardCharsets.UTF_8).contains(WeatherApp.API_KEY_ENV));
        assertEquals(0, outBuffer.size());
    }

    @Test
    public void failsWithUsageCodeOnInvalidArgs() {
        int exitCode = WeatherApp.run(new String[]{"NaN"}, Map.of(WeatherApp.API_KEY_ENV, "key"), out, err);

        assertEquals(WeatherApp.EXIT_USAGE, exitCode);
        assertEquals(0, outBuffer.size());
    }

    @Test
    public void resolvesApiKeyStrippingWhitespace() {
        assertEquals("abc", WeatherApp.resolveApiKey(Map.of(WeatherApp.API_KEY_ENV, "  abc \n")));
        assertThrows(ConfigurationException.class,
                () -> WeatherApp.resolveApiKey(Map.of(WeatherApp.API_KEY_ENV, "   ")));
    }
}
