package ru.mephi.weather;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherRequestTest {

    @Test
    void usesDefaultsWithoutArgs() {
        WeatherRequest expected = new WeatherRequest(
                WeatherRequest.DEFAULT_LATITUDE, WeatherRequest.DEFAULT_LONGITUDE, WeatherRequest.DEFAULT_DAYS);

        assertEquals(expected, WeatherRequest.fromArgs());
    }

    @Test
    void parsesArgs() {
        assertEquals(new WeatherRequest(59.94, 30.31, 5), WeatherRequest.fromArgs("59.94", "30.31", "5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "NaN", "Infinity", "-Infinity", "90.01", "-90.01"})
    void rejectsInvalidLatitude(String latitude) {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs(latitude));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "180.5", "-181"})
    void rejectsInvalidLongitude(String longitude) {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs("55", longitude));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.5", "x"})
    void rejectsInvalidDays(String days) {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs("55", "37", days));
    }

    @Test
    void rejectsTooManyArgs() {
        assertThrows(ConfigurationException.class, () -> WeatherRequest.fromArgs("55", "37", "7", "1"));
    }

    @Test
    void constructorRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherRequest(Double.NaN, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new WeatherRequest(0, 0, 0));
    }
}
