package ru.mephi.weather;

import java.net.URI;
import java.util.Objects;

public record ForecastResponse(URI requestUri, String body) {

    public ForecastResponse {
        Objects.requireNonNull(requestUri, "requestUri");
        Objects.requireNonNull(body, "body");
    }
}
