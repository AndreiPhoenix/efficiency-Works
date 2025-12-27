#!/bin/bash

# Start all services
docker-compose up -d

echo "Services starting..."
echo "Prometheus: http://localhost:9090"
echo "Grafana: http://localhost:3000 (admin/admin)"
echo "Jaeger: http://localhost:16686"
echo "PostgreSQL: localhost:5432"