# mephi-java

Домашние задания по Java. Многомодульный Gradle-проект, каждое задание лежит в своем модуле.

## Модули

| Модуль | Описание | Документация |
|---|---|---|
| `calculator` | Консольный калькулятор с операциями `+ - * /`, сбросом и выходом | [calculator/README.md](calculator/README.md) |
| `weather` | Сервис получения метеоданных из API Яндекс Погоды | [weather/README.md](weather/README.md) |

Инструкции по запуску каждого модуля описаны в его README.

## Требования

- JDK 21
- Gradle устанавливать не нужно, используется Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Сборка

```bash
./gradlew build
```

Команда собирает все модули и запускает тесты. Jar-файлы появляются в `<модуль>/build/libs/`.

Сборка и запуск отдельного модуля:

```bash
./gradlew :calculator:build
./gradlew :weather:run
```

На Windows вместо `./gradlew` можно использовать `gradlew.bat`.
