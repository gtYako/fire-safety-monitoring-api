# Скриншоты запуска на виртуальной машине

Проект развёрнут на **Ubuntu 25.04** в **Oracle VirtualBox 7.2.8** на Windows 10.

| Файл | Описание |
|------|----------|
| `01_virtualbox_manager.jpg` | Oracle VirtualBox Менеджер — виртуальная машина Ubuntu создана и запущена (4096 МБ RAM, 2 ЦПУ, 25 ГБ диск) |
| `02_java_docker_maven_versions.jpg` | Терминал Ubuntu — установленные версии: Java 21, Docker, Maven 3.9 |
| `03_git_clone.jpg` | Клонирование репозитория с GitHub: `git clone https://github.com/gtYako/fire-safety-monitoring-api.git` |
| `04_docker_compose_up.jpg` | Запуск PostgreSQL через Docker Compose — контейнер `fire_safety_postgres` запущен |
| `05_spring_boot_started.jpg` | Запуск Spring Boot приложения — `Started FireSafetyApplication in ~12 seconds`, DataInitializer создал тестовые данные |
| `06_swagger_ui.jpg` | Swagger UI открыт в Firefox внутри виртуальной машины по адресу `http://localhost:8080/swagger-ui/index.html` |
| `07_get_buildings_200ok.jpg` | GET /api/buildings — успешный запрос с JWT токеном, ответ 200 OK со списком корпусов |
