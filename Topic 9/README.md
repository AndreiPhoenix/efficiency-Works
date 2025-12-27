# Инструкции по запуску:

## 1 Сборка проекта:
```
mvn clean compile
```

## 2 Генерация тестовых данных:
```
mvn exec:java -Dexec.mainClass="com.example.demo.domain.DataGenerator"
```

## 3 Запуск бенчмарков:
```
mvn spring-boot:run
```
или
```
mvn spring-boot:run
```

## 4 Запуск тестов:
```
mvn test
```