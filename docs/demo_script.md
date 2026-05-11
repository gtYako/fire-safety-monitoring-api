# Сценарий демонстрации системы

## Порядок записи видео

### 1. GitHub (30 сек)
- Открыть https://github.com/gtYako/fire-safety-monitoring-api
- Показать структуру репозитория, историю коммитов
- Показать README.md

### 2. Структура проекта (30 сек)
- Открыть в IDE или проводнике
- Показать пакеты: controller, service, repository, entity, security

### 3. Запуск PostgreSQL
```bash
docker compose up -d
docker ps  # убедиться, что контейнер запущен
```

### 4. Запуск Spring Boot
```bash
mvn spring-boot:run
# или
java -jar target/fire-safety-monitoring-api-1.0.0.jar
```
Показать логи запуска: инициализация данных, порт 8080

### 5. Swagger UI
- Открыть http://localhost:8080/swagger-ui/index.html
- Показать все группы API

### 6. Авторизация (JWT)
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```
- Скопировать token
- Нажать "Authorize" в Swagger, вставить token

### 7. CRUD — Buildings
```
GET /api/buildings         → список корпусов (уже есть тестовый)
POST /api/buildings        → создать новый корпус
PUT /api/buildings/{id}    → изменить
DELETE /api/buildings/{id} → удалить
```

### 8. CRUD — Sensors
```
GET /api/sensors → список датчиков (5 тестовых)
```

### 9. Создание показания с превышением порога
```json
POST /api/readings
{
  "sensorId": 1,
  "value": 99.9
}
```
Показать в ответе: `"exceeded": true, "alertId": 1`

### 10. Тревога создана
```
GET /api/alerts → показать новую тревогу со статусом NEW
GET /api/alerts/1 → детали тревоги
```

### 11. Telegram-уведомление
- Открыть Telegram-бот @fire_safety_monitor_kppo_bot
- Показать полученное сообщение с информацией о тревоге

### 12. Обработка тревоги
```json
PUT /api/alerts/1/status
{
  "status": "IN_PROGRESS"
}
```

### 13. Создание инцидента
```json
POST /api/incidents
{
  "alertId": 1,
  "description": "Обнаружено задымление в аудитории 101",
  "responsibleUserId": 3
}
```

### 14. Загрузка фото к инциденту
```
POST /api/incidents/1/photos
form-data: file=<photo.jpg>
```

### 15. Импорт датчиков из CSV
```
POST /api/import/sensors/csv
form-data: file=sensors.csv
```
Показать результат: importedCount, failedCount

### 16. Формирование PDF-отчёта
```
GET /api/reports/alerts/pdf?dateFrom=2024-01-01T00:00:00&dateTo=2030-12-31T23:59:59
```
Скачать и открыть PDF

### 17. Формирование XLSX-отчёта
```
GET /api/reports/alerts/xlsx?dateFrom=2024-01-01T00:00:00&dateTo=2030-12-31T23:59:59
```
Скачать и открыть Excel

### 18. Telegram-логи
```
GET /api/telegram/logs
```
Показать записи о отправленных уведомлениях

### 19. Логи приложения
- Открыть `logs/fire-safety.log`
- Показать записи о создании тревоги, отправке Telegram

### 20. База данных
```sql
SELECT * FROM alerts ORDER BY created_at DESC LIMIT 5;
SELECT * FROM telegram_logs ORDER BY sent_at DESC LIMIT 5;
SELECT * FROM incident_photos;
```

### 21. Проверка Security
- Попробовать запрос без токена → 403
- Авторизоваться как technician → нет доступа к DELETE sensors
- Показать @PreAuthorize в коде

### 22. Финал
- Показать все таблицы в БД
- Показать статистику: корпуса, помещения, датчики, тревоги
