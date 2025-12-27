# Database Comparison Project

Сравнение производительности PostgreSQL и MongoDB для сущности ExchangeRate (курсы валют).

## 🚀 Запуск проекта

### 1. Запуск баз данных

```
docker-compose up -d
```

### 2. Запуск приложения
```
mvn spring-boot:run
```

### 3. Доступные интерфейсы
Приложение: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

Actuator: http://localhost:8080/actuator

PostgreSQL Admin (pgAdmin): http://localhost:5050

MongoDB Admin (mongo-express): http://localhost:8081