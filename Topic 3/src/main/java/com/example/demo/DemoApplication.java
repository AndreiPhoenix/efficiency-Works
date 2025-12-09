package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Добавляем тестовые данные
        userRepository.save(new User("Иван Иванов", "ivan@test.com", 25));
        userRepository.save(new User("Петр Петров", "petr@test.com", 30));
        userRepository.save(new User("Мария Сидорова", "maria@test.com", 22));
        userRepository.save(new User("Алексей Алексеев", "alex@test.com", 35));
        userRepository.save(new User("Елена Еленова", "elena@test.com", 28));

        System.out.println("Приложение запущено! API доступно по адресу: http://localhost:8080");
    }
}
