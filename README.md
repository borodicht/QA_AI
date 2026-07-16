# AI QA Pipeline

Функционал:

1. Берем короткий чек-лист.
2. Отправляем его в LLM и получаем тестовые сценарии.
3. Преобразуем сценарии в JSON с тест-кейсами.
4. Забираем HTML целевой страницы.
5. Просим LLM сгенерировать Page Object под эту страницу.
6. Просим LLM сгенерировать UI-автотесты на Selenium/TestNG.
7. Запускаем сгенерированные тесты.
8. Собираем Allure results и лог прогона.
9. На основе лога и Allure результатов просим LLM сформировать bug report.

Проект специально сделан не как production-решение, а как демонстрация того, как можно собрать AI-инструмент из нескольких простых этапов.

## Что умеет приложение

Проект выполняет следующие функции:

- читает чек-лист из файла `checklist.txt`
- генерирует текстовые тестовые сценарии через Mistral API
- генерирует JSON с тест-кейсами
- скачивает HTML целевой страницы по URL из `application.properties`
- генерирует Java Page Object под конкретную страницу
- генерирует Java UI-тесты под конкретные тест-кейсы и Page Object
- запускает только сгенерированный AI-тест
- сохраняет лог выполнения тестов
- собирает результаты Allure
- формирует bug report через LLM на основе:
  - лога прогона
  - файлов `target/allure-results/*-result.json`
- выполняет базовую проверку и маскирование PII в промпте

## Как устроен pipeline

Основной запуск идет через класс `pipeline.PipelineMain`.

Маршрут данных такой:

1. `checklist.txt` читается как входной чек-лист.
2. Шаблон `prompts/01_scenarios_from_checklist.txt` объединяется с чек-листом.
3. Получившийся промпт проверяется на PII.
4. Mistral возвращает тестовые сценарии.
5. Шаблон `prompts/02_testcases_json.txt` превращает сценарии в JSON тест-кейсов.
6. Из `application.properties` читается `application.url`.
7. По этому URL загружается HTML страницы.
8. Шаблон `prompts/03_page_object_java.txt` генерирует `GeneratedPageObject.java`.
9. Вспомогательный базовый класс теста создается автоматически как `GeneratedBaseUiTest.java`.
10. Шаблон `prompts/04_autotests_java.txt` генерирует `GeneratedUiTest.java`.
11. Выполняется Maven-команда запуска только этого теста.
12. Генерируется Allure report.
13. Шаблон `prompts/05_bug_report.txt` получает только лог прогона и результаты Allure.
14. Mistral возвращает bug report в JSON.

## Что нужно для запуска

Минимальные требования:

- Java 17
- Maven 3.9+
- Google Chrome
- интернет-доступ
- ключ Mistral API

Важно:

- проект делает HTTP-запросы к Mistral API
- проект загружает HTML целевой страницы по URL
- UI-тесты запускаются через Selenium и Chrome в headless-режиме

## Быстрый старт

### 1. Установить переменную окружения с ключом

```zsh
export MISTRAL_API_KEY='your-mistral-key'
```

Проверить, что переменная установилась:

```zsh
echo $MISTRAL_API_KEY
```

### 2. Указать тестируемый URL

Открыть файл `application.properties` и задать:

```properties
application.url=https://www.saucedemo.com/
```

Можно подставить любой другой URL страницы, для которой хотите показать генерацию Page Object и тестов.

### 3. Выбрать LLM-провайдера

По умолчанию проект запускается через cloud Mistral по API-ключу:

```properties
llm.provider=mistral
```

При таком режиме:

- используется `MISTRAL_API_KEY` из окружения
- если `llm.baseUrl` не задан, берется `https://api.mistral.ai`
- если `llm.model` не задан, берется `mistral-small-latest`

Для локального запуска через Ollama укажите в `application.properties`:

```properties
llm.provider=ollama
llm.baseUrl=http://localhost:11434
llm.model=gemma4:latest
```

При режиме `ollama` ключ `MISTRAL_API_KEY` не требуется.

### 4. Подготовить чек-лист

Входной чек-лист лежит в файле:

`checklist.txt`

Сейчас в проекте используется именно это имя файла. Pipeline читает его напрямую.

### 5. Запустить pipeline

```zsh
mvn compile exec:java
```

Если все настроено правильно, pipeline:

- создаст промежуточные артефакты в `generated/`
- сгенерирует Java-код тестов
- запустит автотест
- соберет Allure
- сформирует bug report

## Что делать, если pipeline не запускается

### Ошибка `MISTRAL_API_KEY not set`

Причина: выбран `llm.provider=mistral`, но не установлена переменная окружения.

Решение:

```zsh
export MISTRAL_API_KEY='your-mistral-key'
```

После этого повторно выполнить:

```zsh
mvn compile exec:java
```

Если вы хотите запускаться без API-ключа, переключите конфиг на Ollama:

```properties
llm.provider=ollama
llm.baseUrl=http://localhost:11434
llm.model=gemma4:latest
```

### Ошибка при генерации UI-тестов или Page Object

Чаще всего причины такие:

- целевая страница не открывается по `application.url`
- HTML страницы слишком нестандартный
- LLM вернула код, который не проходит внутреннюю валидацию

Проверять в первую очередь нужно файлы в `generated/`:

- `generated/page_snapshot.html`
- `generated/page_object_llm.txt`
- `generated/autotests_llm.txt`

### UI-тесты не стартуют

Проверить:

- установлен ли Google Chrome
- доступен ли запуск headless Chrome
- нет ли ограничений среды на запуск браузера

## Как запускать тесты отдельно

Есть два разных сценария:

- `mvn compile exec:java` запускает весь AI pipeline: генерацию сценариев, тест-кейсов, Page Object, UI-теста, прогон теста, Allure и bug report
- `mvn test` запускает Maven-тесты проекта

Важно:

- внутри pipeline для прогона используется `mvn -Dtest=org.demo.generated.GeneratedUiTest test`
- при обычном `mvn test` Maven запускает все доступные тесты проекта, а не только шаги AI pipeline

## Allure

Результаты Allure складываются в:

`target/allure-results`

Настройка директории задается в файле:

`src/test/resources/allure.properties`

Для сборки HTML-отчета:

```zsh
mvn allure:report
```

Готовый HTML-отчет будет в:

`target/site/allure-maven-plugin`

## Что генерируется автоматически

### Java-файлы

Pipeline создает или перезаписывает:

- `src/test/java/org/demo/generated/support/GeneratedBaseUiTest.java`
- `src/test/java/org/demo/generated/pages/GeneratedPageObject.java`
- `src/test/java/org/demo/generated/GeneratedUiTest.java`

### Промежуточные артефакты

В папке `generated/` создаются файлы, которые удобно смотреть по шагам:

- `generated/scenarios_prompt.txt` - промпт на генерацию сценариев
- `generated/scenarios_prompt_masked.txt` - версия промпта после маскировки PII, если были найденные данные
- `generated/pii_report.txt` - отчет по найденным PII
- `generated/scenarios_raw.json` - сырой ответ Mistral со сценариями
- `generated/scenarios.txt` - извлеченный текст сценариев
- `generated/testcases_prompt.txt` - промпт на генерацию тест-кейсов
- `generated/testcases_raw.json` - сырой ответ Mistral по тест-кейсам
- `generated/testcases_llm.txt` - текст ответа LLM по тест-кейсам
- `generated/testcases.json` - итоговый JSON с тест-кейсами
- `generated/page_snapshot.html` - HTML страницы, который использовался как контекст
- `generated/page_object_prompt.txt` - промпт на генерацию Page Object
- `generated/page_object_raw.json` - сырой ответ Mistral по Page Object
- `generated/page_object_llm.txt` - текст ответа LLM по Page Object
- `generated/autotests_prompt.txt` - промпт на генерацию UI-тестов
- `generated/autotests_raw.json` - сырой ответ Mistral по тестам
- `generated/autotests_llm.txt` - текст ответа LLM по тестам
- `generated/test_run.log` - лог прогона Maven-тестов
- `generated/allure_report.log` - лог сборки Allure report
- `generated/allure_results.txt` - объединенные данные из `target/allure-results/*-result.json`
- `generated/bug_report_prompt.txt` - промпт на генерацию bug report
- `generated/bug_report_raw.json` - сырой ответ Mistral по bug report
- `generated/bug_report_llm.txt` - текст ответа LLM по bug report
- `generated/bug_report.json` - итоговый bug report в JSON

## Структура проекта

### Корень проекта

- `application.properties` - URL тестируемого приложения
- `checklist.txt` - входной чек-лист
- `pom.xml` - Maven-конфигурация
- `prompts/` - шаблоны промптов для LLM
- `generated/` - все промежуточные и итоговые артефакты pipeline

### `src/main/java`

- `pipeline/PipelineMain.java` - основная логика AI pipeline и точка входа
- `pipeline/PromptEngine.java` - сборка промптов из шаблонов и чек-листа
- `client/MistralClient.java` - клиент работы с Mistral API через RestAssured
- `config/ApplicationConfig.java` - чтение `application.properties`
- `ui/UiSnapshotClient.java` - загрузка HTML страницы по URL
- `generator/GeneratedTestSupport.java` - создание базового класса UI-теста
- `generator/PageObjectGenerator.java` - генерация Page Object
- `generator/TestGenerator.java` - генерация UI-тестов
- `generator/JavaCodeExtractor.java` - извлечение Java-кода из ответа LLM
- `parser/JsonExtractor.java` - извлечение JSON из ответа LLM
- `parser/TestcasesParser.java` - разбор JSON тест-кейсов
- `model/` - простые модели тест-кейсов
- `security/PiiScanner.java` - поиск чувствительных данных
- `security/PiiMasker.java` - маскирование чувствительных данных
- `security/PiiPatterns.java` - шаблоны для PII
- `security/PiiReport.java` - отчет по найденным PII
- `util/FilesUtil.java` - чтение, запись, очистка директорий
- `util/TestRunExecutor.java` - запуск Maven-тестов и сборка Allure

### `src/test/java`

- `org/demo/generated/` - AI-сгенерированные UI-тесты
- `org/demo/generated/pages/` - AI-сгенерированный Page Object
- `org/demo/generated/support/` - базовый класс для AI-теста
- `security/PiiScannerTest.java` - unit-тест на PII scanner

## Какие промпты есть в проекте

- `prompts/01_scenarios_from_checklist.txt` - из чек-листа в сценарии
- `prompts/02_testcases_json.txt` - из сценариев в JSON тест-кейсов
- `prompts/03_page_object_java.txt` - генерация Page Object по URL и HTML
- `prompts/04_autotests_java.txt` - генерация UI-тестов по тест-кейсам и Page Object
- `prompts/05_bug_report.txt` - из логов и Allure в bug report

Все промпты написаны на русском языке.

## Ограничения

Этот проект показывает идею, а не гарантирует 100% стабильную генерацию.

Основные ограничения:

- качество результата зависит от промптов
- качество результата зависит от HTML целевой страницы
- LLM может сгенерировать код, который придется доработать
- UI-тесты чувствительны к изменениям интерфейса
- bug report формируется только по логу прогона и данным Allure, без дополнительных артефактов

Это показывает, где AI помогает, а где нужны инженерные ограничения, валидация и контроль качества.

## Рекомендуемый сценарий демонстрации

1. Показать `checklist.txt`.
2. Показать `application.properties`.
3. Установить `MISTRAL_API_KEY`.
4. Запустить pipeline одной командой.
5. Открыть папку `generated/` и пройтись по этапам.
6. Показать сгенерированный `GeneratedPageObject.java`.
7. Показать сгенерированный `GeneratedUiTest.java`.
8. Показать `generated/test_run.log`.
9. Показать `target/allure-results` и HTML-отчет Allure.
10. Показать итоговый `generated/bug_report.json`.

## Команды

Основная команда:

```zsh
mvn compile exec:java
```

Дополнительные команды:

```zsh
mvn test
mvn allure:report
```

## Безопасность

- не храните `MISTRAL_API_KEY` в коде
- не коммитьте ключ в Git
- не добавляйте секреты в чек-листы и промпты
- pipeline умеет искать и маскировать часть PII, но это не полноценная DLP-защита
