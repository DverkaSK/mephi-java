package ru.mephi.weather;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WeatherRequestTest {

    @Test
    public void usesDefaultsWithoutArgs() {
        WeatherRequest expected = new WeatherRequest(
                WeatherRequest.DEFAULT_LATITUDE, WeatherRequest.DEFAULT_LONGITUDE, WeatherRequest.DEFAULT_DAYS);

        assertEquals(expected, WeatherRequest.fromArgs());
    }

    @Test
    public void parsesArgs() {
        assertEquals(new WeatherRequest(59.94, 30.31, 5), WeatherRequest.fromArgs("59.94", "30.31", "5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "NaN", "Infinity", "-Infinity", "90.01", "-90.01"})
    public void rejectsInvalidLatitude(String latitude) {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs(latitude));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "180.5", "-181"})
    public void rejectsInvalidLongitude(String longitude) {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs("55", longitude));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.5", "x"})
    public void rejectsInvalidDays(String days) {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs("55", "37", days));
    }

    @Test
    public void rejectsTooManyArgs() {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs("55", "37", "7", "1"));
    }

    @Test
    public void constructorRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherRequest(Double.NaN, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new WeatherRequest(0, 0, 0));
    }
}
