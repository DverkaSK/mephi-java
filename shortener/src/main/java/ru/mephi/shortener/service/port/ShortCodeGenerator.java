package ru.mephi.shortener.service.port;

@FunctionalInterface
public interface ShortCodeGenerator {

    String nextCode();
}
