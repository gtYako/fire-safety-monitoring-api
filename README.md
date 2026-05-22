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

### 2. Настройка переменных окружения (опционально)

Создайте файл `.env` (не коммитить в git!):
```
TELEGRAM_BOT_TOKEN=ваш_токен_бота
TELEGRAM_CHAT_ID=856994240
```

### 3. Запуск приложения

```bash
mvn spring-boot:run
```

Для запуска через PowerShell с Maven Wrapper и Telegram:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
$env:TELEGRAM_BOT_TOKEN="ваш_токен_бота"
$env:TELEGRAM_CHAT_ID="856994240"
.\mvnw.cmd spring-boot:run
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
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "roles": ["ADMIN"]
}
```

В Swagger нажмите **Authorize** и введите токен.

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
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"sensorId":1,"value":99.9}'
```

Если значение превышает порог датчика — автоматически создаётся Alert и отправляется Telegram-уведомление.

### Загрузка фото к инциденту

```bash
curl -X POST http://localhost:8080/api/incidents/1/photos \
  -H "Authorization: Bearer <token>" \
  -F "file=@photo.jpg"
```

### Импорт датчиков из CSV

CSV-формат: `inventoryNumber,type,status,roomId,thresholdValue`

```bash
curl -X POST http://localhost:8080/api/import/sensors/csv \
  -H "Authorization: Bearer <token>" \
  -F "file=@docs/examples/sensors.csv"
```

### Формирование отчёта PDF

```bash
curl -X GET "http://localhost:8080/api/reports/alerts/pdf?dateFrom=2024-01-01T00:00:00&dateTo=2030-12-31T23:59:59" \
  -H "Authorization: Bearer <token>" \
  --output report.pdf
```

### Формирование отчёта XLSX

```bash
curl -X GET "http://localhost:8080/api/reports/alerts/xlsx?dateFrom=2024-01-01T00:00:00&dateTo=2030-12-31T23:59:59" \
  -H "Authorization: Bearer <token>" \
  --output report.xlsx
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
2. Выполните `POST /api/auth/login` → скопируйте token
3. Нажмите **Authorize**, введите token
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
