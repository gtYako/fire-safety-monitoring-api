# Логическая схема базы данных

## Описание сущностей и связей

### Основные сущности

| Сущность | Описание |
|----------|----------|
| User | Пользователь системы |
| Role | Роль пользователя |
| Permission | Право доступа |
| Building | Учебный корпус |
| Room | Помещение в корпусе |
| Sensor | Датчик пожарной безопасности |
| SensorReading | Показание датчика |
| Alert | Тревога при превышении порога |
| Incident | Инцидент, связанный с тревогой |
| IncidentPhoto | Фото к инциденту |
| TelegramLog | Лог Telegram-уведомлений |
| ImportLog | Лог импорта CSV |
| ReportLog | Лог формирования отчётов |

### Связи

- **User ↔ Role**: многие-ко-многим (пользователь имеет несколько ролей)
- **Role ↔ Permission**: многие-ко-многим (роль имеет несколько прав)
- **Building → Room**: один-ко-многим (корпус содержит помещения)
- **Room → Sensor**: один-ко-многим (помещение содержит датчики)
- **Sensor → SensorReading**: один-ко-многим (датчик имеет показания)
- **Sensor → Alert**: один-ко-многим (датчик создаёт тревоги)
- **SensorReading → Alert**: один-к-одному (показание вызывает тревогу)
- **Alert → Incident**: один-ко-многим (тревога порождает инцидент)
- **User → Incident**: один-ко-многим (пользователь ответственен за инциденты)
- **Incident → IncidentPhoto**: один-ко-многим (инцидент имеет фото)
- **User → ReportLog**: один-ко-многим (пользователь генерирует отчёты)

## ERD-диаграмма (Mermaid)

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar username UK
        varchar password
        varchar fullName
        varchar email UK
        boolean enabled
    }
    ROLE {
        bigint id PK
        varchar name UK
    }
    PERMISSION {
        bigint id PK
        varchar name UK
    }
    BUILDING {
        bigint id PK
        varchar name
        varchar address
        text description
    }
    ROOM {
        bigint id PK
        varchar number
        int floor
        varchar purpose
        bigint building_id FK
    }
    SENSOR {
        bigint id PK
        varchar inventoryNumber UK
        varchar type
        varchar status
        bigint room_id FK
        timestamp installedAt
        double thresholdValue
    }
    SENSOR_READING {
        bigint id PK
        bigint sensor_id FK
        double value
        timestamp measuredAt
    }
    ALERT {
        bigint id PK
        bigint sensor_id FK
        bigint reading_id FK
        varchar alertType
        text message
        varchar status
        timestamp createdAt
        timestamp resolvedAt
    }
    INCIDENT {
        bigint id PK
        bigint alert_id FK
        text description
        bigint responsible_user_id FK
        varchar status
        timestamp createdAt
        timestamp closedAt
    }
    INCIDENT_PHOTO {
        bigint id PK
        bigint incident_id FK
        varchar originalFileName
        varchar storedFileName
        varchar filePath
        varchar contentType
        bigint size
        timestamp uploadedAt
    }
    TELEGRAM_LOG {
        bigint id PK
        text message
        varchar status
        timestamp sentAt
        text errorText
    }
    IMPORT_LOG {
        bigint id PK
        varchar fileName
        int importedCount
        int failedCount
        timestamp importedAt
        varchar status
        text errors
    }
    REPORT_LOG {
        bigint id PK
        varchar reportType
        varchar fileName
        timestamp generatedAt
        bigint generated_by FK
    }

    USER }o--o{ ROLE : "user_roles"
    ROLE }o--o{ PERMISSION : "role_permissions"
    BUILDING ||--o{ ROOM : contains
    ROOM ||--o{ SENSOR : "installed in"
    SENSOR ||--o{ SENSOR_READING : "has readings"
    SENSOR ||--o{ ALERT : "triggers"
    SENSOR_READING ||--o| ALERT : "causes"
    ALERT ||--o{ INCIDENT : "creates"
    USER ||--o{ INCIDENT : "responsible for"
    INCIDENT ||--o{ INCIDENT_PHOTO : "has photos"
    USER ||--o{ REPORT_LOG : generates
```

## Роли и права доступа

| Роль | Права |
|------|-------|
| ADMIN | Все права |
| DISPATCHER | BUILDING_READ, ROOM_READ, SENSOR_READ, READING_READ, READING_CREATE, ALERT_READ, ALERT_UPDATE, INCIDENT_READ, INCIDENT_UPDATE, REPORT_GENERATE |
| TECHNICIAN | BUILDING_READ, ROOM_READ, SENSOR_READ, READING_READ, ALERT_READ, INCIDENT_READ, INCIDENT_UPDATE, FILE_UPLOAD |
| VIEWER | BUILDING_READ, ROOM_READ, SENSOR_READ, READING_READ, ALERT_READ, INCIDENT_READ |
