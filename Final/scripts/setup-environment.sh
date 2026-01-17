#!/bin/bash

echo "================================================"
echo "Performance Analysis Environment Setup"
echo "================================================"

# Создание необходимых директорий
echo "Creating directories..."
mkdir -p grafana/provisioning/dashboards
mkdir -p grafana/provisioning/datasources
mkdir -p grafana/dashboards
mkdir -p prometheus
mkdir -p benchmark-results/gc-logs
mkdir -p docs/images
mkdir -p docs/reports
mkdir -p logs
mkdir -p init-db

echo "Creating configuration files..."

# Prometheus конфигурация
cat > prometheus/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    monitor: 'performance-analysis'

scrape_configs:
  - job_name: 'performance-analysis-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
        labels:
          application: 'performance-analysis'
          environment: 'development'
    scrape_interval: 10s
    scrape_timeout: 5s

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
EOF

# Grafana datasource конфигурация
cat > grafana/provisioning/datasources/datasource.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: 5s
      queryTimeout: 60s
      httpMethod: POST
EOF

# Grafana dashboard конфигурация
cat > grafana/provisioning/dashboards/dashboard.yml << 'EOF'
apiVersion: 1

providers:
  - name: 'Performance Dashboards'
    orgId: 1
    folder: 'Performance Analysis'
    type: file
    disableDeletion: false
    editable: true
    options:
      path: /etc/grafana/provisioning/dashboards
EOF

# SQL init файл
cat > init-db/init.sql << 'EOF'
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Создание таблицы для мониторинга производительности
CREATE TABLE IF NOT EXISTS performance_metrics (
    id SERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tags JSONB
);

CREATE INDEX idx_performance_metrics_timestamp ON performance_metrics(timestamp);
CREATE INDEX idx_performance_metrics_name ON performance_metrics(metric_name);
EOF

# Promtail конфигурация
cat > promtail-config.yml << 'EOF'
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
- job_name: app-logs
  static_configs:
  - targets:
      - localhost
    labels:
      job: performance-app
      __path__: /var/log/*.log
EOF

echo "Making scripts executable..."
chmod +x scripts/*.sh

echo "================================================"
echo "Environment setup completed!"
echo ""
echo "Next steps:"
echo "1. Run: docker-compose up -d"
echo "2. Run: mvn clean package"
echo "3. Run: java -jar target/performance-analysis-1.0.0.jar"
echo "4. Run: ./scripts/run-benchmarks.sh"
echo "================================================"