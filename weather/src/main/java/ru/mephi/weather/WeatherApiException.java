package ru.mephi.weather;

import java.io.Serial;

public final class WeatherApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String responseBody;

    public WeatherApiException(int statusCode, String responseBody) {
        super("Сервис вернул HTTP " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
