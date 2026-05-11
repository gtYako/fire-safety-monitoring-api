# Физическая схема базы данных (PostgreSQL)

## Таблицы

### users
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| username | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| full_name | VARCHAR(255) | |
| email | VARCHAR(255) | UNIQUE |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE |

### roles
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| name | VARCHAR(255) | NOT NULL, UNIQUE |

### permissions
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| name | VARCHAR(255) | NOT NULL, UNIQUE |

### user_roles (join table)
| Поле | Тип | Ограничения |
|------|-----|-------------|
| user_id | BIGINT | FK → users(id) |
| role_id | BIGINT | FK → roles(id) |

### role_permissions (join table)
| Поле | Тип | Ограничения |
|------|-----|-------------|
| role_id | BIGINT | FK → roles(id) |
| permission_id | BIGINT | FK → permissions(id) |

### buildings
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| name | VARCHAR(255) | NOT NULL |
| address | VARCHAR(255) | NOT NULL |
| description | TEXT | |

### rooms
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| number | VARCHAR(255) | NOT NULL |
| floor | INTEGER | NOT NULL |
| purpose | VARCHAR(255) | |
| building_id | BIGINT | FK → buildings(id), NOT NULL |

### sensors
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| inventory_number | VARCHAR(255) | NOT NULL, UNIQUE |
| type | VARCHAR(50) | NOT NULL (SMOKE/TEMPERATURE/GAS/MANUAL_BUTTON) |
| status | VARCHAR(50) | NOT NULL (ACTIVE/INACTIVE/MAINTENANCE/BROKEN) |
| room_id | BIGINT | FK → rooms(id), NOT NULL |
| installed_at | TIMESTAMP | NOT NULL |
| threshold_value | FLOAT8 | NOT NULL |

### sensor_readings
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| sensor_id | BIGINT | FK → sensors(id), NOT NULL |
| value | FLOAT8 | NOT NULL |
| measured_at | TIMESTAMP | NOT NULL |

### alerts
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| sensor_id | BIGINT | FK → sensors(id), NOT NULL |
| reading_id | BIGINT | FK → sensor_readings(id) |
| alert_type | VARCHAR(50) | NOT NULL |
| message | TEXT | |
| status | VARCHAR(50) | NOT NULL (NEW/IN_PROGRESS/RESOLVED/FALSE_ALARM) |
| created_at | TIMESTAMP | NOT NULL |
| resolved_at | TIMESTAMP | |

### incidents
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| alert_id | BIGINT | FK → alerts(id), NOT NULL |
| description | TEXT | |
| responsible_user_id | BIGINT | FK → users(id) |
| status | VARCHAR(50) | NOT NULL (OPEN/IN_PROGRESS/CLOSED) |
| created_at | TIMESTAMP | NOT NULL |
| closed_at | TIMESTAMP | |

### incident_photos
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| incident_id | BIGINT | FK → incidents(id), NOT NULL |
| original_file_name | VARCHAR(255) | NOT NULL |
| stored_file_name | VARCHAR(255) | NOT NULL |
| file_path | VARCHAR(255) | NOT NULL |
| content_type | VARCHAR(100) | NOT NULL |
| size | BIGINT | NOT NULL |
| uploaded_at | TIMESTAMP | NOT NULL |

### telegram_logs
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| message | TEXT | NOT NULL |
| status | VARCHAR(50) | NOT NULL (SUCCESS/FAILED/SKIPPED) |
| sent_at | TIMESTAMP | NOT NULL |
| error_text | TEXT | |

### import_logs
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| file_name | VARCHAR(255) | NOT NULL |
| imported_count | INTEGER | NOT NULL, DEFAULT 0 |
| failed_count | INTEGER | NOT NULL, DEFAULT 0 |
| imported_at | TIMESTAMP | NOT NULL |
| status | VARCHAR(50) | NOT NULL (SUCCESS/PARTIAL/FAILED) |
| errors | TEXT | |

### report_logs
| Поле | Тип | Ограничения |
|------|-----|-------------|
| id | BIGSERIAL | PK |
| report_type | VARCHAR(50) | NOT NULL (ALERT_PDF/ALERT_XLSX) |
| file_name | VARCHAR(255) | NOT NULL |
| generated_at | TIMESTAMP | NOT NULL |
| generated_by | BIGINT | FK → users(id) |

## ERD-диаграмма (физическая)

```mermaid
erDiagram
    users {
        bigserial id PK
        varchar username UK
        varchar password
        varchar full_name
        varchar email UK
        boolean enabled
    }
    roles { bigserial id PK; varchar name UK }
    permissions { bigserial id PK; varchar name UK }
    user_roles { bigint user_id FK; bigint role_id FK }
    role_permissions { bigint role_id FK; bigint permission_id FK }
    buildings { bigserial id PK; varchar name; varchar address; text description }
    rooms { bigserial id PK; varchar number; int floor; varchar purpose; bigint building_id FK }
    sensors { bigserial id PK; varchar inventory_number UK; varchar type; varchar status; bigint room_id FK; timestamp installed_at; float8 threshold_value }
    sensor_readings { bigserial id PK; bigint sensor_id FK; float8 value; timestamp measured_at }
    alerts { bigserial id PK; bigint sensor_id FK; bigint reading_id FK; varchar alert_type; text message; varchar status; timestamp created_at; timestamp resolved_at }
    incidents { bigserial id PK; bigint alert_id FK; text description; bigint responsible_user_id FK; varchar status; timestamp created_at; timestamp closed_at }
    incident_photos { bigserial id PK; bigint incident_id FK; varchar original_file_name; varchar stored_file_name; varchar file_path; varchar content_type; bigint size; timestamp uploaded_at }
    telegram_logs { bigserial id PK; text message; varchar status; timestamp sent_at; text error_text }
    import_logs { bigserial id PK; varchar file_name; int imported_count; int failed_count; timestamp imported_at; varchar status; text errors }
    report_logs { bigserial id PK; varchar report_type; varchar file_name; timestamp generated_at; bigint generated_by FK }

    users }o--o{ user_roles : ""
    roles }o--o{ user_roles : ""
    roles }o--o{ role_permissions : ""
    permissions }o--o{ role_permissions : ""
    buildings ||--o{ rooms : ""
    rooms ||--o{ sensors : ""
    sensors ||--o{ sensor_readings : ""
    sensors ||--o{ alerts : ""
    sensor_readings ||--o| alerts : ""
    alerts ||--o{ incidents : ""
    users ||--o{ incidents : ""
    incidents ||--o{ incident_photos : ""
    users ||--o{ report_logs : ""
```
