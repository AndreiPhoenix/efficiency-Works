# Performance Analysis Project

Проект для анализа производительности многопоточного приложения.

## Структура проекта
- `src/main/java` - исходный код приложения
- `src/main/resources` - конфигурационные файлы
- `scripts/` - скрипты для запуска
- `grafana/` - конфигурация Grafana

## Запуск проекта

### 1. Зависимости
- Java 17+
- Docker и Docker Compose
- Gradle

### 2. Запуск инфраструктуры
```
chmod +x scripts/start-prometheus.sh
./scripts/start-prometheus.sh
```

### 3. Сборка и запуск приложения
```
./gradlew bootRun
```

### 4. JMH бенчмаркинг
```
./gradlew jmh
```

### Java Flight Recorder
```
# Запуск с JFR
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
     -jar build/libs/performance-analysis-1.0.0.jar

# Анализ записи
jfr print recording.jfr
```