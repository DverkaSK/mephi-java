package ru.mephi.weather;

public final class TestData {

    public static final String FORECAST_RESPONSE = """
            {
              "now": 1729425600,
              "fact": { "temp": 7, "feels_like": 3, "condition": "cloudy" },
              "forecasts": [
                { "date": "2026-10-20", "parts": { "day": { "temp_avg": 8 }, "night": { "temp_avg": 2 } } },
                { "date": "2026-10-21", "parts": { "day": { "temp_avg": 6 }, "night": { "temp_avg": 1 } } },
                { "date": "2026-10-22", "parts": { "day": { "temp_avg": 3 }, "night": { "temp_avg": -1 } } }
              ]
            }
            """;

    private TestData() {
    }
}
