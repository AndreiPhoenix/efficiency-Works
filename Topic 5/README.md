# Vacancy Parser - REST vs WebSocket Comparison

## Описание проекта
Микросервис для парсинга вакансий с двумя методами клиент-серверного взаимодействия:
1. **REST API с polling** (опрос каждые 500 мс)
2. **WebSocket** (push-уведомления)

## Технологии
- Java 11
- Spring Boot 2.7.14
- WebSocket (STOMP)
- HTML5 + JavaScript (чистый, без фреймворков)
- JMeter 5.5 (для нагрузочного тестирования)

## Запуск проекта

### 1. Сборка и запуск
```bash
mvn clean install
mvn spring-boot:run
```