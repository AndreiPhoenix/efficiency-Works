# Redis Caching Demo Project

## Описание проекта
Демонстрационный проект для снижения нагрузки на базу данных и ускорения ответов сервиса с использованием Redis для кэширования.

## Технологии
- Java 11
- Spring Boot 2.7
- Spring Data JPA
- Spring Cache + Redis
- H2 Database
- Docker + Docker Compose
- Redis Commander (Web UI)

## Запуск проекта

### 1. Сборка проекта
```bash
mvn clean package
```

### 2. Запуск через Docker Compose
```
docker-compose up -d
```

### 3. Запуск без Docker
```
# Запустите Redis
docker run -p 6379:6379 --name redis-cache -d redis:7-alpine

# Запустите приложение
mvn spring-boot:run
```