# Fire Safety Monitoring API

Backend REST API системы мониторинга пожарной безопасности учебных корпусов.

## Описание проекта

Система предназначена для учёта учебных корпусов, помещений, датчиков пожарной безопасности, показаний датчиков, тревог и инцидентов. При обнаружении опасных показаний система автоматически создаёт тревогу, логирует событие и отправляет уведомление в Telegram.

## Стек технологий

- **Java 17** + **Spring Boot 3.2**
- **Spring Data JPA** + **PostgreSQL**
- **Spring Security** + **JWT**
- **Apache POI** (XLSX отчёты)
- **OpenPDF** (PDF отчёты)
- **OpenCSV** (импорт датчиков)
- **Telegram Bot API** (уведомления)
- **Swagger/OpenAPI** (документация)
- **Docker Compose** (PostgreSQL)
- **JUnit 5 + Mockito** (тесты)

## Функциональные возможности

- Полный CRUD для корпусов, помещений, датчиков, пользователей
- Автоматическое создание тревоги при превышении порога датчика
- Telegram-уведомления о тревогах
- Управление инцидентами с прикреплением фото
- Импорт датчиков из CSV
- Формирование отчётов PDF и XLSX
- JWT-аутентификация с ролевой системой
- Логирование всех событий

## Роли и права доступа

| Роль | Описание | Ключевые права |
|------|----------|----------------|
| ADMIN | Полный доступ | Все права |
| DISPATCHER | Диспетчер | Просмотр датчиков, создание показаний, обработка тревог, отчёты |
| TECHNICIAN | Техник | Просмотр инцидентов, изменение статуса, загрузка фото |
| VIEWER | Наблюдатель | Только чтение справочной информации |

## Быстрый старт

### 1. Запуск PostgreSQL

```bash
docker compose up -d
```

### Требования к запуску

- **Java 17 или 21** (рекомендуется Java 21 LTS — Lombok не совместим с Java 25)
- **Maven 3.9+** (или используйте mvnw.cmd из проекта)
- **Docker** для PostgreSQL

Если у вас Java 25, установите Java 21 LTS:
- Скачать: https://adoptium.net/temurin/releases/?version=21
- Установить и задать `JAVA_HOME` перед запуском

### 2. Настройка Telegram (опционально)

Telegram-токен и chat_id задаются через переменные окружения. Не добавляйте реальный токен в git.

```powershell
$env:TELEGRAM_BOT_TOKEN="123456789:AA_example_bot_token"
$env:TELEGRAM_CHAT_ID="856994240"
```

Если Telegram не нужен, эти переменные можно не задавать. Приложение запустится, а уведомления в Telegram будут пропущены.

### 3. Запуск приложения

Запуск через PowerShell с Maven Wrapper:

```powershell
$env:JAVA_HOME=(Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1).FullName
$env:TELEGRAM_BOT_TOKEN="123456789:AA_example_bot_token"
$env:TELEGRAM_CHAT_ID="856994240"
.\mvnw.cmd spring-boot:run
```

Если Java 21 установлена в другой папке, укажите точный путь к JDK вместо команды поиска.

Если используется Java 25, для сборки с тестами нужен дополнительный параметр:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
.\mvnw.cmd -DargLine="-Dnet.bytebuddy.experimental=true" clean package
```

### 4. Swagger UI

Откройте: http://localhost:8080/swagger-ui/index.html

## Аутентификация

### Получение JWT-токена

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Ответ:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "roles": ["ADMIN"]
}
```

`accessToken` используется в Swagger **Authorize** и в заголовке `Authorization: Bearer <accessToken>`.
`refreshToken` используется только для обновления токенов:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

Ответ refresh возвращает новый `accessToken` и новый `refreshToken`.

В Swagger нажмите **Authorize** и введите accessToken.

### Краткая проверка требований

1. `POST /api/auth/login` с `admin/admin123` должен вернуть `accessToken`, `refreshToken`, `tokenType`.
2. В Swagger **Authorize** вставьте `accessToken`, затем вызовите `GET /api/buildings` - должен быть `200 OK`.
3. `POST /api/auth/refresh` с `refreshToken` должен вернуть новую пару `accessToken` и `refreshToken`.
4. Если вставить `refreshToken` в Swagger **Authorize** и вызвать `GET /api/buildings`, должен быть отказ `401` или `403`.
5. `POST /api/auth/logout` с текущим `refreshToken` должен вернуть `204 No Content`; повторный refresh с этим токеном должен завершиться ошибкой.

## Тестовые пользователи

| Имя пользователя | Пароль | Роль |
|-----------------|--------|------|
| admin | admin123 | ADMIN |
| dispatcher | disp123 | DISPATCHER |
| technician | tech123 | TECHNICIAN |

## Примеры запросов

### Создание показания с превышением порога

```bash
curl -X POST http://localhost:8080/api/readings \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"sensorId":1,"value":99.9}'
```

Если значение превышает порог датчика — автоматически создаётся Alert и отправляется Telegram-уведомление.

### Загрузка фото к инциденту

```bash
curl -X POST http://localhost:8080/api/incidents/1/photos \
  -H "Authorization: Bearer <accessToken>" \
  -F "file=@photo.jpg"
```

### Импорт датчиков из CSV

CSV-формат: `inventoryNumber,type,status,roomId,thresholdValue`

```bash
curl -X POST http://localhost:8080/api/import/sensors/csv \
  -H "Authorization: Bearer <accessToken>" \
  -F "file=@docs/examples/sensors.csv"
```

### Формирование отчёта PDF

```bash
curl -X GET "http://localhost:8080/api/reports/alerts/pdf?dateFrom=2024-01-01T00:00:00&dateTo=2030-12-31T23:59:59" \
  -H "Authorization: Bearer <accessToken>" \
  --output report.pdf
```

### Формирование отчёта XLSX

```bash
curl -X GET "http://localhost:8080/api/reports/alerts/xlsx?dateFrom=2024-01-01T00:00:00&dateTo=2030-12-31T23:59:59" \
  -H "Authorization: Bearer <accessToken>" \
  --output report.xlsx
```

### JSON-превью отчёта

Для проверки структуры отчётов есть DTO в пакете `dto/report`: `ReportRequest`, `ReportResponse`, `AlertReportRow`.

```bash
curl -X POST http://localhost:8080/api/reports/alerts/preview \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"dateFrom":"2024-01-01T00:00:00","dateTo":"2030-12-31T23:59:59"}'
```

## Проверка Telegram-уведомлений

### Создание Telegram-бота

1. Найдите @BotFather в Telegram
2. Отправьте `/newbot`
3. Укажите имя и username бота
4. Скопируйте токен

### Получение CHAT_ID

1. Напишите боту любое сообщение
2. Откройте: `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates`
3. Найдите `"id"` в `"chat"` объекте

### Настройка

Добавьте в `.env`:
```
TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
TELEGRAM_CHAT_ID=856994240
```

Если токен не настроен — приложение запустится без ошибок и запишет предупреждение в лог.

## Проверка CRUD через Swagger

1. Откройте http://localhost:8080/swagger-ui/index.html
2. Выполните `POST /api/auth/login` → скопируйте accessToken
3. Нажмите **Authorize**, введите accessToken
4. Тестируйте любые endpoint'ы

## Запуск тестов

```bash
mvn clean test
```

## Сборка

```bash
mvn clean package
java -jar target/fire-safety-monitoring-api-1.0.0.jar
```

## Связь с темами курса КППО

| Тема курса | Применение в проекте |
|-----------|---------------------|
| Maven/сборка | pom.xml, mvn spring-boot:run |
| HTTP/REST | 12 групп endpoint'ов |
| DI/IoC | @Service, @Repository, @RequiredArgsConstructor |
| Spring Data JPA | @Entity, JpaRepository, @Transactional |
| Spring Security | JWT, BCrypt, @PreAuthorize |
| Файлы/MIME | FileStorageService, multipart/form-data |
| Логирование | SLF4J, файл logs/fire-safety.log |
| Swagger | springdoc-openapi |
| Docker | docker-compose.yml |
| Telegram | TelegramService, @Async |
| Отчёты | Apache POI (XLSX), OpenPDF (PDF) |
| Тесты | JUnit 5, Mockito |

## Ответы на вопросы по проекту

Подробные ответы на экзаменационные вопросы: [docs/tickets_project_answers.md](docs/tickets_project_answers.md)

## Документация

- [Логическая схема БД](docs/database_logical_schema.md)
- [Физическая схема БД](docs/database_physical_schema.md)
- [Сценарий демонстрации](docs/demo_script.md)
- [Отчёт по курсу](docs/report_kppo.md)
- [Пример CSV для импорта](docs/examples/sensors.csv)
