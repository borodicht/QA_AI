# AI QA Pipeline

Учебный проект, демонстрирующий AI-конвейер для QA: от чек-листа до тест-кейсов, Selenium/TestNG автотестов, code review и bug report.

## Запуск

Требуется Java 17, Maven, Google Chrome и ключ Mistral API.

```zsh
export MISTRAL_API_KEY='your-key'
mvn compile exec:java -Dexec.mainClass=pipeline.PipelineMain
```

Результаты каждого этапа сохраняются в `generated/`. Сгенерированный AI Java-тест записывается в `src/test/java/org/demo/generated/GeneratedLoginTest.java`. После генерации pipeline запускает `mvn test`, сохраняет лог в `generated/test_run.log`, строит Allure-отчёт и передаёт в AI bug report только лог прогона и результаты Allure.

Для запуска UI-тестов:

```zsh
mvn test
```

Результаты Allure сохраняются в `target/allure-results`. Построить HTML-отчёт можно командой:

```zsh
mvn allure:report
```

Готовый отчёт будет находиться в `target/site/allure-maven-plugin`.

Не добавляйте API-ключ в исходный код, README или Git.
