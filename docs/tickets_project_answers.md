# Ответы на вопросы экзаменационных билетов по проекту

## Билет 1 — Maven/Gradle, XML/JSON/YAML

**Maven vs Gradle:**
В проекте используется Maven. Структура: `pom.xml` в корне, `src/main/java`, `src/main/resources`, `src/test`. Maven выбран за простоту конфигурации через XML и лучшую интеграцию с Spring Boot Starter Parent.

**Форматы данных:**
- `application.yml` — конфигурация сервера (YAML: удобочитаем, поддерживает иерархию)
- REST API возвращает JSON (автоматически через Jackson в Spring Web)
- Импорт датчиков — CSV формат (`inventoryNumber,type,status,roomId,thresholdValue`)
- Swagger документация — JSON/YAML через springdoc-openapi

## Билет 2 — HTTP, клиент-сервер

**HTTP-методы в API:**
- `GET /api/alerts` — получить список тревог (идемпотентный, безопасный)
- `POST /api/readings` — создать показание датчика (не идемпотентный)
- `PUT /api/alerts/{id}/status` — обновить статус (идемпотентный)
- `DELETE /api/buildings/{id}` — удалить корпус

**TCP/IP и производительность:**
Приложение работает на локальном порту 8080. Для продакшена рекомендуется HTTP/2 через nginx reverse proxy для устранения head-of-line blocking.

## Билет 3 — DI/IoC в Spring

**IoC в проекте:**
```java
@Service
@RequiredArgsConstructor  // Lombok генерирует конструктор для DI
public class SensorReadingService {
    private final SensorRepository sensorRepository; // Spring внедряет зависимость
    private final AlertService alertService;          // через конструктор
}
```
Spring контейнер управляет жизненным циклом всех бинов. Тестирование упрощается: можно заменить зависимости на моки (@Mock в тестах).

## Билет 4 — Threading, race conditions

**Потенциальные race conditions в проекте:**
При одновременном создании показаний несколькими диспетчерами возможна дублирующая генерация тревог.

**Решение:**
```java
// В SensorReadingService.create() используется @Transactional
// Это гарантирует атомарность: чтение+запись в одной транзакции
@Transactional
public SensorReadingResponse create(SensorReadingRequest request) { ... }
```

Telegram-уведомления выполняются асинхронно `@Async` чтобы не блокировать основной поток.

## Билет 5 — @RequestParam, @PathVariable

**Примеры из проекта:**
```java
// @PathVariable — идентификатор ресурса в URL
@GetMapping("/{id}")
public ResponseEntity<AlertResponse> getById(@PathVariable Long id) { ... }

// @RequestParam — параметры фильтрации
@GetMapping("/alerts/pdf")
public ResponseEntity<byte[]> alertsPdf(
    @RequestParam @DateTimeFormat(iso = DATE_TIME) LocalDateTime dateFrom,
    @RequestParam @DateTimeFormat(iso = DATE_TIME) LocalDateTime dateTo) { ... }
```

Пароли не передаются в GET — они отправляются в теле POST-запроса через HTTPS.

## Билет 6 — CRUD, DAO, REST

**CRUD на примере Sensor:**
- Create: `POST /api/sensors` → `SensorService.create()`
- Read: `GET /api/sensors/{id}` → `SensorService.getById()`
- Update: `PUT /api/sensors/{id}` → `SensorService.update()`
- Delete: `DELETE /api/sensors/{id}` → `SensorService.delete()`

**DAO/Repository:**
`SensorRepository extends JpaRepository<Sensor, Long>` — изолирует логику БД от бизнес-логики. Service не знает SQL, только вызывает методы репозитория.

**Идемпотентность:** `PUT /api/alerts/{id}/status` — повторный вызов с тем же статусом даст тот же результат.

## Билет 7 — Spring Data JPA, ACID

**Примеры сущностей:**
```java
@Entity
@Table(name = "sensors")
public class Sensor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}
```

**Транзакции:**
```java
@Transactional  // Гарантирует ACID при создании reading + alert
public SensorReadingResponse create(SensorReadingRequest request) {
    SensorReading reading = readingRepository.save(reading);
    if (exceeded) alertService.createAlert(sensor, reading); // атомарно
}
```

## Билет 8 — N+1 problem, кэширование

**N+1 в проекте:**
При `sensorRepository.findAll()` и последующем обращении к `sensor.getRoom().getBuilding()` — каждый вызов генерирует доп. SQL.

**Решение:**
```java
// В UserRepository используется @EntityGraph
@EntityGraph(attributePaths = {"roles", "roles.permissions"})
Optional<User> findByUsername(String username);
```

Для датчиков — LAZY loading + `fetch = FetchType.LAZY` позволяет избежать лишних JOIN. Кэширование на уровне приложения можно добавить через Spring Cache + Redis.

## Билет 9 — Статические файлы, MIME-типы, multipart

**Загрузка файлов в проекте:**
```java
// POST /api/incidents/{id}/photos
// Content-Type: multipart/form-data
private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
    "image/jpeg", "image/png", "image/gif", "image/webp"
);
```

Проверяется MIME-тип через `file.getContentType()`, размер ≤ 10MB. Файлы сохраняются с UUID-именем в папку `uploads/`.

## Билет 10 — Spring Security, BCrypt, JWT, CSRF

**Security в проекте:**
- Пароли: `passwordEncoder.encode(password)` → BCrypt с солью
- JWT: stateless, хранится на клиенте (в Postman/Swagger как Bearer token)
- CSRF: отключён (`csrf.disable()`) так как API stateless (нет куки сессий)
- `@PreAuthorize("hasAuthority('SENSOR_DELETE')")` — проверка прав на уровне метода

## Билет 11 — JWT, асинхронность

**JWT:**
- Генерируется при логине: `POST /api/auth/login` → возвращает token
- Передаётся в заголовке: `Authorization: Bearer <token>`
- Проверяется в `JwtAuthenticationFilter` на каждый запрос

**Асинхронность:**
```java
@Async  // Telegram-уведомление не блокирует ответ API
public void sendAlert(String message) { ... }
```
`@EnableAsync` в `FireSafetyApplication` активирует пул потоков для @Async методов.

## Билет 12 — Kafka (теоретически)

Kafka могла бы использоваться для:
- Публикации события при создании тревоги в топик `alerts`
- `@KafkaListener` для обработки тревог: отправка Telegram, создание инцидента
- Гарантированная доставка при сбоях (fault tolerance через репликацию)
- Партиционирование по building_id для параллельной обработки

В текущей реализации логика синхронная, что подходит для учебного проекта.

## Билет 13 — JUnit, Mockito, логирование

**Тесты:**
```java
@ExtendWith(MockitoExtension.class)
class SensorServiceTest {
    @Mock SensorRepository sensorRepository;
    @InjectMocks SensorService sensorService;

    @Test
    void createSensor_duplicateInventoryNumber_throwsException() { ... }
}
```

**Логирование (SLF4J + Logback):**
```java
log.info("Creating sensor: {}", inventoryNumber);  // INFO для бизнес-событий
log.warn("Threshold exceeded: {}", value);         // WARN для тревог
log.error("Failed: {}", e.getMessage(), e);        // ERROR для ошибок
```

## Билет 14 — Docker, CI/CD

**docker-compose.yml:**
```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: fire_safety_db
    ports: ["5432:5432"]
```

**Запуск:**
```bash
docker compose up -d   # запустить PostgreSQL
mvn spring-boot:run    # запустить приложение
```

CI/CD pipeline мог бы выглядеть: push → GitHub Actions → mvn test → mvn package → docker build → deploy.

## Билет 15 — Мониторинг, Prometheus, Grafana

**Для развития проекта:**
- Добавить `spring-boot-starter-actuator` → метрики доступны на `/actuator/metrics`
- Prometheus scrape → Grafana dashboards
- Метрики: количество тревог/минуту, время ответа API, ошибки БД
- Алерты: uptime < 99.9% → уведомление в Telegram/PagerDuty

Текущий проект логирует все события через SLF4J → файл `logs/fire-safety.log`.

## Билет 16 — Роли Scrum

**Применение Scrum к проекту:**
- **Product Owner** — преподаватель (определил требования и критерии сдачи)
- **Scrum Master** — ведущий разработчик (устраняет технические блокеры)
- **Developers** — команда разработчиков (реализуют REST API, Security, отчёты)

Product Backlog включал: базовый CRUD, Security, Telegram, отчёты, Docker. Каждая функция — отдельный элемент Backlog.

## Билет 17 — События Scrum

**Применение:**
- **Sprint Planning**: выбрать задачи (например, "реализовать Security + JWT")
- **Daily Scrum**: что сделано вчера (entities), что сегодня (services), блокеры (JJWT API)
- **Sprint Review**: демонстрация рабочего Swagger и CRUD
- **Sprint Retrospective**: ускорить разработку через Lombok @Builder, избежать повторения кода

Эмпиризм: после каждого Sprint можно адаптировать требования (например, добавить пагинацию после review).
