package ru.mephi.weather;

public record WeatherRequest(double latitude, double longitude, int days) {

    public static final double DEFAULT_LATITUDE = 55.75;
    public static final double DEFAULT_LONGITUDE = 37.62;
    public static final int DEFAULT_DAYS = 7;

    private static final int MAX_ARGS = 3;

    public WeatherRequest {
        requireInRange("Широта", latitude, 90);
        requireInRange("Долгота", longitude, 180);
        if (days < 1) {
            throw new IllegalArgumentException("Количество дней должно быть положительным: " + days);
        }
    }

    public static WeatherRequest fromArgs(String... args) {
        if (args.length > MAX_ARGS) {
            throw new ConfigurationException("Ожидается не более трех аргументов: lat lon limit");
        }
        try {
            return new WeatherRequest(
                    args.length > 0 ? Double.parseDouble(args[0]) : DEFAULT_LATITUDE,
                    args.length > 1 ? Double.parseDouble(args[1]) : DEFAULT_LONGITUDE,
                    args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_DAYS);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Некорректные аргументы: " + e.getMessage(), e);
        }
    }

    private static void requireInRange(String name, double value, double bound) {
        if (!Double.isFinite(value) || value < -bound || value > bound) {
            throw new IllegalArgumentException(
                    "%s должна быть в диапазоне [%s; %s]: %s".formatted(name, -bound, bound, value));
        }
    }
}
