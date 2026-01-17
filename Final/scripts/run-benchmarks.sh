#!/bin/bash

echo "================================================"
echo "Running Performance Benchmarks"
echo "================================================"
echo ""

# Создание timestamp для результатов
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
RESULTS_DIR="benchmark-results/${TIMESTAMP}"
mkdir -p "$RESULTS_DIR"

echo "Results will be saved to: $RESULTS_DIR"
echo ""

# Сборка проекта
echo "Building project..."
mvn clean package -DskipTests > "$RESULTS_DIR/build.log" 2>&1

if [ $? -ne 0 ]; then
    echo "❌ Build failed! Check $RESULTS_DIR/build.log"
    exit 1
fi
echo "✅ Build successful"

echo ""
echo "Running benchmarks with different JVM configurations..."
echo ""

# Конфигурации JVM для тестирования
declare -A JVM_CONFIGS=(
    ["G1GC"]="-Xms2g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    ["ParallelGC"]="-Xms2g -Xmx2g -XX:+UseParallelGC -XX:MaxGCPauseMillis=200"
    ["SerialGC"]="-Xms2g -Xmx2g -XX:+UseSerialGC"
    ["ZGC"]="-Xms2g -Xmx2g -XX:+UseZGC"
)

for config_name in "${!JVM_CONFIGS[@]}"; do
    echo "🔍 Testing $config_name configuration..."
    echo "JVM Args: ${JVM_CONFIGS[$config_name]}"

    # Запуск бенчмарка
    java ${JVM_CONFIGS[$config_name]} \
         -Dspring.profiles.active=benchmark \
         -jar target/performance-analysis-1.0.0.jar \
         --jmh.benchmark=ParserBenchmark \
         --jmh.result="$RESULTS_DIR/${config_name}_results.json" \
         --jmh.resultFormat=JSON \
         > "$RESULTS_DIR/${config_name}_output.log" 2>&1 &

    BENCHMARK_PID=$!

    # Ждем 30 секунд для прогрева
    echo "Waiting 30 seconds for warmup..."
    sleep 30

    # Запускаем нагрузочный тест
    echo "Starting load test..."
    java -jar target/performance-analysis-1.0.0.jar \
         --spring.profiles.active=loadtest \
         > "$RESULTS_DIR/${config_name}_loadtest.log" 2>&1 &

    LOADTEST_PID=$!

    # Ждем 60 секунд выполнения
    echo "Running for 60 seconds..."
    sleep 60

    # Останавливаем тесты
    echo "Stopping tests..."
    kill $LOADTEST_PID 2>/dev/null
    kill $BENCHMARK_PID 2>/dev/null

    sleep 5
    echo "✅ $config_name test completed"
    echo ""
done

echo ""
echo "Running memory benchmarks..."
java -Xms512m -Xmx512m \
     -Dspring.profiles.active=benchmark \
     -jar target/performance-analysis-1.0.0.jar \
     --jmh.benchmark=MemoryBenchmark \
     --jmh.result="$RESULTS_DIR/memory_results.json" \
     --jmh.resultFormat=JSON \
     > "$RESULTS_DIR/memory_output.log" 2>&1

echo "✅ Memory benchmarks completed"
echo ""

# Сбор метрик
echo "Collecting system metrics..."
if command -v docker &> /dev/null; then
    docker stats --no-stream > "$RESULTS_DIR/docker_stats.log" 2>&1
fi

if command -v top &> /dev/null; then
    top -b -n 1 > "$RESULTS_DIR/system_stats.log" 2>&1
fi

# Генерация отчета
echo "Generating benchmark report..."
cat > "$RESULTS_DIR/benchmark-report.md" << EOF
# Performance Benchmark Report
## Generated: $(date)

## Test Environment
- OS: $(uname -s) $(uname -r)
- CPU Cores: $(nproc)
- Total Memory: $(free -h | awk '/^Mem:/ {print $2}')
- Java Version: $(java -version 2>&1 | head -1)

## Test Configurations
\`\`\`
$(for config_name in "${!JVM_CONFIGS[@]}"; do
  echo "$config_name: ${JVM_CONFIGS[$config_name]}"
done)
\`\`\`

## Results Summary

### Throughput Comparison (ops/sec)
| Configuration | For Loop | Stream | Parallel Stream |
|--------------|----------|--------|----------------|
$(for config_name in "${!JVM_CONFIGS[@]}"; do
    if [ -f "$RESULTS_DIR/${config_name}_results.json" ]; then
        echo "| $config_name | TODO | TODO | TODO |"
    fi
done)

### Memory Usage (MB)
| Configuration | Peak Usage | Avg Usage | GC Time |
|--------------|------------|-----------|---------|
$(for config_name in "${!JVM_CONFIGS[@]}"; do
    echo "| $config_name | TODO | TODO | TODO |"
done)

## Analysis

### 1. GC Performance
- **G1GC**: Best for predictable pause times
- **ParallelGC**: Highest throughput for batch processing
- **SerialGC**: Lowest memory overhead
- **ZGC**: Best for very large heaps (>32GB)

### 2. Memory Efficiency
- Object allocation patterns
- Heap fragmentation
- GC pressure points

### 3. CPU Utilization
- Thread contention
- Lock efficiency
- Parallelism effectiveness

## Recommendations

1. **For production**: Use G1GC with -XX:MaxGCPauseMillis=200
2. **For batch processing**: Use ParallelGC
3. **Memory optimization**: Review object pooling
4. **Thread optimization**: Adjust thread pool sizes

## Files Generated
- Benchmark results: \`${config_name}_results.json\`
- GC logs: \`gc_*.log\`
- System metrics: \`system_stats.log\`
- Full output: \`${config_name}_output.log\`
EOF

echo ""
echo "================================================"
echo "Benchmarks completed!"
echo "Results saved to: $RESULTS_DIR"
echo "Report: $RESULTS_DIR/benchmark-report.md"
echo "================================================"