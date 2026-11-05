# mephi-java

[![build](https://github.com/DverkaSK/mephi-java/actions/workflows/build.yml/badge.svg)](https://github.com/DverkaSK/mephi-java/actions/workflows/build.yml)

Домашние задания по Java. Многомодульный Gradle-проект, каждое задание лежит в своем модуле.

## Модули

| Модуль | Описание | Документация |
|---|---|---|
| `calculator` | Консольный калькулятор с операциями `+ - * /`, сбросом и выходом | [calculator/README.md](calculator/README.md) |
| `weather` | Сервис получения метеоданных из API Яндекс Погоды | [weather/README.md](weather/README.md) |
| `shortener` | Консольный сервис сокращения ссылок с лимитом переходов, временем жизни и правами владельца | [shortener/README.md](shortener/README.md) |

Инструкции по запуску каждого модуля описаны в его README.

## Требования

- JDK 21
- Gradle устанавливать не нужно, используется Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Сборка

```bash
./gradlew build
```

Команда собирает все модули, проверяет стиль кода (Checkstyle) и запускает тесты. Jar-файлы появляются в `<модуль>/build/libs/`.

Сборка и запуск отдельного модуля:

```bash
./gradlew :calculator:build
./gradlew :weather:run
```

На Windows вместо `./gradlew` можно использовать `gradlew.bat`.

## Качество кода и CI

- [`.editorconfig`](.editorconfig) задает кодировку, отступы и переводы строк для IDE
- Checkstyle подключен ко всем модулям через общий плагин сборки, правила лежат в [`config/checkstyle/checkstyle.xml`](config/checkstyle/checkstyle.xml); проверка запускается отдельно командой `./gradlew check`
- Компиляция идет с `-Xlint:all -Werror`, любое предупреждение компилятора ломает сборку
- [GitHub Actions](.github/workflows/build.yml) на каждый push и pull request выполняет `./gradlew build`: сборку, Checkstyle и тесты всех модулей
