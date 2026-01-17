#!/bin/bash

echo "================================================"
echo "Starting Performance Analysis Environment"
echo "================================================"

# Проверка зависимостей
echo "Checking dependencies..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed!"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed!"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed!"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed!"
    exit 1
fi

echo "✅ All dependencies satisfied"
echo ""

# Запуск инфраструктуры
echo "Starting Docker services..."
docker-compose up -d

echo "Waiting for services to start..."
sleep 10

# Проверка сервисов
echo ""
echo "Checking service status..."

check_service() {
    local service=$1
    local port=$2

    if docker-compose ps | grep -q "$service.*Up"; then
        echo "✅ $service is running (port: $port)"
        return 0
    else
        echo "❌ $service failed to start"
        return 1
    fi
}

check_service "postgres" "5432"
check_service "prometheus" "9090"
check_service "grafana" "3000"
check_service "jaeger" "16686"

echo ""
echo "Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful"
echo ""

echo "Starting application..."
java -jar target/performance-analysis-1.0.0.jar &
APP_PID=$!

echo "Application started with PID: $APP_PID"
echo "Waiting for application to initialize..."
sleep 15

echo ""
echo "================================================"
echo "Environment is ready!"
echo "================================================"
echo ""
echo "Services:"
echo "  Prometheus:  http://localhost:9090"
echo "  Grafana:     http://localhost:3000"
echo "               Username: admin"
echo "               Password: admin"
echo "  Jaeger:      http://localhost:16686"
echo "  Application: http://localhost:8080"
echo "  Actuator:    http://localhost:8080/actuator"
echo ""
echo "Quick commands:"
echo "  Monitor:     ./scripts/monitor-performance.sh"
echo "  Benchmarks:  ./scripts/run-benchmarks.sh"
echo "  Stop all:    docker-compose down && kill $APP_PID"
echo ""
echo "To import Grafana dashboards:"
echo "  1. Go to http://localhost:3000"
echo "  2. Login with admin/admin"
echo "  3. Import dashboard ID: 4701 (JVM Micrometer)"
echo "  4. Import dashboard ID: 6756 (Spring Boot)"
echo ""
echo "Press Ctrl+C to stop all services"
echo "================================================"

# Ожидание Ctrl+C
trap "echo 'Shutting down...'; docker-compose down; kill $APP_PID; exit 0" INT

while true; do
    sleep 1
done