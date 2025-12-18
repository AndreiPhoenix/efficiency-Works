package com.example.currency;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ДЕМОНСТРАЦИЯ ПРОБЛЕМ ПРОИЗВОДИТЕЛЬНОСТИ");
        System.out.println("=".repeat(60));

        if (args.length > 0) {
            String demo = args[0];

            switch (demo) {
                case "threads":
                    runThreadPoolDemo();
                    break;
                case "memory":
                    runMemoryLeakDemo();
                    break;
                case "gc":
                    runGCDemo();
                    break;
                case "docker":
                    runDockerDemo();
                    break;
                case "all":
                    runAllDemos();
                    break;
                default:
                    System.out.println("Доступные демонстрации:");
                    System.out.println("  threads - Проблемы с потоками и context switching");
                    System.out.println("  memory  - Утечки памяти");
                    System.out.println("  gc      - Проблемы с Garbage Collector");
                    System.out.println("  docker  - Настройки JVM в контейнерах");
                    System.out.println("  all     - Все демонстрации");
            }
        } else {
            System.out.println("Запустите Spring Boot с аргументом для демонстрации:");
            System.out.println("  java -jar target/currency-converter-1.0.0.jar threads");
            System.out.println("Или запустите демо-классы напрямую:");
            System.out.println("  java -cp target/classes com.example.currency.ThreadPoolDemo");
            System.out.println("  java -cp target/classes com.example.currency.MemoryLeakDemo");
        }
    }

    private void runThreadPoolDemo() throws InterruptedException {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ДЕМОНСТРАЦИЯ: ПРОБЛЕМЫ С ПОТОКАМИ");
        System.out.println("=".repeat(60));

        // Используем статические методы или создаем экземпляр
        ThreadPoolDemo demo = new ThreadPoolDemo();
        demo.demonstrateContextSwitchingProblem(500);
        demo.demonstrateGoodPool(500);
        demo.demonstrateBackpressure();
    }

    private void runMemoryLeakDemo() throws InterruptedException {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ДЕМОНСТРАЦИЯ: УТЕЧКИ ПАМЯТИ");
        System.out.println("=".repeat(60));

        MemoryLeakDemo demo = new MemoryLeakDemo();
        demo.demonstrateCacheLeak();
        demo.demonstrateListenerLeak();
    }

    private void runGCDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ДЕМОНСТРАЦИЯ: GARBAGE COLLECTOR");
        System.out.println("=".repeat(60));

        GCPerformanceDemo demo = new GCPerformanceDemo();
        demo.demonstrateGCImpact();
        demo.demonstrateDifferentGCTypes();
    }

    private void runDockerDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ДЕМОНСТРАЦИЯ: DOCKER И JVM");
        System.out.println("=".repeat(60));

        DockerJvmTuningDemo demo = new DockerJvmTuningDemo();
        demo.printJvmInfo();
        demo.demonstrateContainerAwareness();
    }

    private void runAllDemos() throws InterruptedException {
        runThreadPoolDemo();
        runMemoryLeakDemo();
        runGCDemo();
        runDockerDemo();
    }
}