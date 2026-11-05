package ru.mephi.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForecastAnalyzerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void extractsCurrentTemperature() throws Exception {
        JsonNode root = MAPPER.readTree(TestData.FORECAST_RESPONSE);

        assertEquals(7, ForecastAnalyzer.currentTemperature(root).orElseThrow().intValue());
    }

    @Test
    public void currentTemperatureIsEmptyWhenFactMissing() throws Exception {
        assertTrue(ForecastAnalyzer.currentTemperature(MAPPER.readTree("{}")).isEmpty());
    }

    @Test
    public void currentTemperatureIsEmptyWhenNotNumber() throws Exception {
        assertTrue(ForecastAnalyzer.currentTemperature(MAPPER.readTree("{\"fact\": {\"temp\": \"7\"}}")).isEmpty());
    }

    @Test
    public void averagesOnlyFirstLimitDays() throws Exception {
        JsonNode root = MAPPER.readTree(TestData.FORECAST_RESPONSE);

        ForecastAnalyzer.AverageTemperature average = ForecastAnalyzer.averageDayTemperature(root, 2).orElseThrow();

        assertEquals(7.0, average.value(), 1e-9);
        assertEquals(2, average.days());
    }

    @Test
    public void usesAvailableDaysWhenLimitExceedsForecast() throws Exception {
        JsonNode root = MAPPER.readTree(TestData.FORECAST_RESPONSE);

        ForecastAnalyzer.AverageTemperature average = ForecastAnalyzer.averageDayTemperature(root, 10).orElseThrow();

        assertEquals(17.0 / 3, average.value(), 1e-9);
        assertEquals(3, average.days());
    }

    @Test
    public void averageIsEmptyWithoutForecasts() throws Exception {
        assertTrue(ForecastAnalyzer.averageDayTemperature(MAPPER.readTree("{\"fact\": {\"temp\": 1}}"), 7).isEmpty());
    }
}
