package com.example.currency;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -cp target/classes com.example.currency.Main <demo>");
            System.out.println("Available demos:");
            System.out.println("  threads - Thread pool issues");
            System.out.println("  memory  - Memory leaks");
            System.out.println("  gc      - GC performance");
            System.out.println("  docker  - Docker & JVM tuning");
            System.out.println("  all     - Run all demos");
            return;
        }

        String demo = args[0];
        switch (demo) {
            case "threads":
                ThreadPoolDemo threadDemo = new ThreadPoolDemo();
                threadDemo.demonstrateContextSwitchingProblem(500);
                threadDemo.demonstrateGoodPool(500);
                threadDemo.demonstrateBackpressure();
                break;

            case "memory":
                MemoryLeakDemo memoryDemo = new MemoryLeakDemo();
                memoryDemo.demonstrateCacheLeak();
                memoryDemo.demonstrateListenerLeak();
                break;

            case "gc":
                GCPerformanceDemo gcDemo = new GCPerformanceDemo();
                gcDemo.demonstrateGCImpact();
                gcDemo.demonstrateDifferentGCTypes();
                break;

            case "docker":
                DockerJvmTuningDemo dockerDemo = new DockerJvmTuningDemo();
                dockerDemo.printJvmInfo();
                dockerDemo.demonstrateContainerAwareness();
                break;

            case "all":
                runAllDemos();
                break;

            default:
                System.out.println("Unknown demo: " + demo);
        }
    }

    private static void runAllDemos() throws Exception {
        System.out.println("=== Running Thread Pool Demo ===");
        ThreadPoolDemo threadDemo = new ThreadPoolDemo();
        threadDemo.demonstrateContextSwitchingProblem(200);
        threadDemo.demonstrateGoodPool(200);

        System.out.println("\n=== Running GC Demo ===");
        GCPerformanceDemo gcDemo = new GCPerformanceDemo();
        gcDemo.demonstrateGCImpact();

        System.out.println("\n=== Running Docker Demo ===");
        DockerJvmTuningDemo dockerDemo = new DockerJvmTuningDemo();
        dockerDemo.printJvmInfo();
    }
}