package com.memory.leak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
public class MemoryLeakApp {

    // Статическая коллекция для создания утечки памяти
    private static final List<byte[]> LEAKING_LIST = new ArrayList<>();

    // Класс с утечкой памяти в методе
    static class LeakyClass {
        private static final List<String> staticList = new ArrayList<>();

        public void addToStaticList(String data) {
            // Утечка: добавление в статическую коллекцию
            staticList.add(data + System.currentTimeMillis());
        }
    }

    private final LeakyClass leakyInstance = new LeakyClass();
    private long requestCount = 0;

    public static void main(String[] args) {
        SpringApplication.run(MemoryLeakApp.class, args);
        System.out.println("Приложение запущено с утечкой памяти!");
        System.out.println("Откройте: http://localhost:8080/leak?size=1024");
    }

    @GetMapping("/leak")
    public String createMemoryLeak(int size) {
        requestCount++;

        // 1. Добавляем данные в статическую коллекцию
        byte[] data = new byte[size * 1024]; // KB
        LEAKING_LIST.add(data);

        // 2. Используем класс с утечкой
        leakyInstance.addToStaticList("Request_" + requestCount);

        // 3. Создаем циклические ссылки (дополнительная утечка)
        createCyclicReferences();

        return String.format("Утечка создана! Добавлено %d KB. Всего объектов: %d",
                size, LEAKING_LIST.size());
    }

    @GetMapping("/info")
    public String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        return String.format("Использовано памяти: %d MB / %d MB. Утечек: %d",
                usedMemory, maxMemory, LEAKING_LIST.size());
    }

    @GetMapping("/clear")
    public String clearMemory() {
        int count = LEAKING_LIST.size();
        LEAKING_LIST.clear();
        System.gc();
        return "Очищено " + count + " объектов. Вызван System.gc()";
    }

    private void createCyclicReferences() {
        // Создаем циклические ссылки для затруднения сборки мусора
        Node node1 = new Node("Node_" + requestCount + "_A");
        Node node2 = new Node("Node_" + requestCount + "_B");

        node1.setNext(node2);
        node2.setNext(node1);

        // Храним в статической мапе
        NodeCache.cache.put("Key_" + requestCount, node1);
    }

    static class Node {
        String name;
        Node next;

        Node(String name) {
            this.name = name;
        }

        void setNext(Node next) {
            this.next = next;
        }
    }

    static class NodeCache {
        static final java.util.Map<String, Node> cache = new java.util.HashMap<>();
    }
}