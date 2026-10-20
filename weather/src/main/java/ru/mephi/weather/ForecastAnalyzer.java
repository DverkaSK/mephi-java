package ru.mephi.weather;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class ForecastAnalyzer {

    private ForecastAnalyzer() {
    }

    public static Optional<Number> currentTemperature(JsonNode forecast) {
        return Optional.of(forecast.path("fact").path("temp"))
                .filter(JsonNode::isNumber)
                .map(JsonNode::numberValue);
    }

    public static Optional<AverageTemperature> averageDayTemperature(JsonNode forecast, int days) {
        DoubleSummaryStatistics statistics = StreamSupport.stream(forecast.path("forecasts").spliterator(), false)
                .limit(days)
                .map(day -> day.path("parts").path("day").path("temp_avg"))
                .filter(JsonNode::isNumber)
                .mapToDouble(JsonNode::asDouble)
                .summaryStatistics();

        return statistics.getCount() == 0
                ? Optional.empty()
                : Optional.of(new AverageTemperature(statistics.getAverage(), Math.toIntExact(statistics.getCount())));
    }

    public record AverageTemperature(double value, int days) {
    }
}
