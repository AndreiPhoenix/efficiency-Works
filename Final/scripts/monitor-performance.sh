#!/bin/bash

echo "================================================"
echo "Performance Monitoring Dashboard"
echo "================================================"
echo ""

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Проверка доступности сервисов
check_service() {
    local name=$1
    local url=$2
    local timeout=${3:-5}

    if curl -s --max-time $timeout "$url" > /dev/null; then
        echo -e "${GREEN}✓${NC} $name is running"
        return 0
    else
        echo -e "${RED}✗${NC} $name is not responding"
        return 1
    fi
}

echo "1. SERVICE STATUS"
echo "-----------------"
check_service "Prometheus" "http://localhost:9090"
check_service "Grafana" "http://localhost:3000"
check_service "Application" "http://localhost:8080/actuator/health"
check_service "Jaeger" "http://localhost:16686"

echo ""
echo "2. APPLICATION METRICS"
echo "----------------------"

METRICS_URL="http://localhost:8080/actuator/prometheus"
if check_service "Metrics" "$METRICS_URL" 2; then
    # Получение метрик
    METRICS=$(curl -s "$METRICS_URL")

    # Парсинг успешных операций
    SUCCESS=$(echo "$METRICS" | grep 'parser_success_total' | tail -1 | awk '{print $2}')
    FAILURE=$(echo "$METRICS" | grep 'parser_failures_total' | tail -1 | awk '{print $2}')

    if [ -n "$SUCCESS" ] && [ -n "$FAILURE" ]; then
        TOTAL=$(echo "$SUCCESS + $FAILURE" | bc)
        if [ "$TOTAL" != "0" ]; then
            SUCCESS_RATE=$(echo "scale=2; $SUCCESS * 100 / $TOTAL" | bc)
            echo -e "Success Rate: ${BLUE}${SUCCESS_RATE}%${NC} (${SUCCESS}/${TOTAL})"
        fi
    fi

    # Время парсинга
    PARSE_TIME=$(echo "$METRICS" | grep 'parser_duration_seconds_sum' | tail -1 | awk '{print $2}')
    PARSE_COUNT=$(echo "$METRICS" | grep 'parser_duration_seconds_count' | tail -1 | awk '{print $2}')

    if [ -n "$PARSE_TIME" ] && [ -n "$PARSE_COUNT" ] && [ "$PARSE_COUNT" != "0" ]; then
        AVG_TIME=$(echo "scale=3; $PARSE_TIME * 1000 / $PARSE_COUNT" | bc)
        echo -e "Average Parse Time: ${BLUE}${AVG_TIME}ms${NC}"
    fi

    # Использование памяти JVM
    HEAP_USED=$(echo "$METRICS" | grep 'jvm_memory_used_bytes{area="heap"}' | tail -1 | awk '{print $2}')
    HEAP_MAX=$(echo "$METRICS" | grep 'jvm_memory_max_bytes{area="heap"}' | tail -1 | awk '{print $2}')

    if [ -n "$HEAP_USED" ] && [ -n "$HEAP_MAX" ] && [ "$HEAP_MAX" != "0" ]; then
        HEAP_PERCENT=$(echo "scale=2; $HEAP_USED * 100 / $HEAP_MAX" | bc)

        if [ $(echo "$HEAP_PERCENT > 80" | bc) -eq 1 ]; then
            COLOR=$RED
        elif [ $(echo "$HEAP_PERCENT > 60" | bc) -eq 1 ]; then
            COLOR=$YELLOW
        else
            COLOR=$GREEN
        fi

        echo -e "Heap Usage: ${COLOR}${HEAP_PERCENT}%${NC}"
    fi

    # GC время
    GC_TIME=$(echo "$METRICS" | grep 'jvm_gc_pause_seconds_sum' | tail -1 | awk '{print $2}')
    if [ -n "$GC_TIME" ]; then
        echo -e "Total GC Time: ${BLUE}${GC_TIME}s${NC}"
    fi

    # Количество потоков
    THREADS=$(echo "$METRICS" | grep 'jvm_threads_live_threads' | tail -1 | awk '{print $2}')
    if [ -n "$THREADS" ]; then
        echo -e "Live Threads: ${BLUE}${THREADS}${NC}"
    fi
else
    echo "Cannot fetch metrics"
fi

echo ""
echo "3. SYSTEM METRICS"
echo "-----------------"

# Использование CPU
if command -v top &> /dev/null; then
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    echo -e "CPU Usage: ${BLUE}${CPU_USAGE}%${NC}"
fi

# Использование памяти
if command -v free &> /dev/null; then
    MEM_TOTAL=$(free -h | awk '/^Mem:/ {print $2}')
    MEM_USED=$(free -h | awk '/^Mem:/ {print $3}')
    echo -e "Memory Used: ${BLUE}${MEM_USED}/${MEM_TOTAL}${NC}"
fi

# Дисковое пространство
if command -v df &> /dev/null; then
    DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}')
    echo -e "Disk Usage: ${BLUE}${DISK_USAGE}${NC}"
fi

echo ""
echo "4. PERFORMANCE ALERTS"
echo "---------------------"

# Проверка проблем
if [ -n "$AVG_TIME" ] && [ $(echo "$AVG_TIME > 100" | bc) -eq 1 ]; then
    echo -e "${YELLOW}⚠️  High parse time detected (>100ms)${NC}"
fi

if [ -n "$HEAP_PERCENT" ] && [ $(echo "$HEAP_PERCENT > 80" | bc) -eq 1 ]; then
    echo -e "${RED}⚠️  High heap usage detected (>80%)${NC}"
fi

if [ -n "$GC_TIME" ] && [ $(echo "$GC_TIME > 10" | bc) -eq 1 ]; then
    echo -e "${RED}⚠️  High GC time detected (>10s)${NC}"
fi

if [ -n "$SUCCESS_RATE" ] && [ $(echo "$SUCCESS_RATE < 95" | bc) -eq 1 ]; then
    echo -e "${YELLOW}⚠️  Low success rate detected (<95%)${NC}"
fi

echo ""
echo "5. QUICK LINKS"
echo "--------------"
echo -e "${BLUE}Prometheus:${NC}     http://localhost:9090"
echo -e "${BLUE}Grafana:${NC}        http://localhost:3000 (admin/admin)"
echo -e "${BLUE}Application:${NC}    http://localhost:8080"
echo -e "${BLUE}Actuator:${NC}       http://localhost:8080/actuator"
echo -e "${BLUE}Metrics:${NC}        http://localhost:8080/actuator/prometheus"
echo -e "${BLUE}Jaeger:${NC}         http://localhost:16686"

echo ""
echo "6. QUICK COMMANDS"
echo "-----------------"
echo "Run benchmarks:    ./scripts/run-benchmarks.sh"
echo "Analyze GC logs:   ./scripts/analyze-gc.sh <file>"
echo "Start services:    docker-compose up -d"
echo "Stop services:     docker-compose down"

echo ""
echo "================================================"
echo "Monitoring active - Press Ctrl+C to exit"
echo "================================================"

# Непрерывный мониторинг
while true; do
    sleep 10
    clear
    $0
done