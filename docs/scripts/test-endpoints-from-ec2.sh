#!/bin/bash

# Script para ejecutar en EC2 y probar los endpoints

echo "🧪 PROBANDO ENDPOINTS DESDE EC2"
echo "================================"
echo ""

echo "1️⃣ Probando /hello (debe ser público)..."
curl -i http://localhost:8080/hello
echo ""
echo ""

echo "2️⃣ Probando /api/v1/products (debe ser público)..."
curl -i http://localhost:8080/api/v1/products
echo ""
echo ""

echo "3️⃣ Probando /actuator/health (debe ser público)..."
curl -i http://localhost:8080/actuator/health
echo ""
echo ""

echo "4️⃣ Probando /swagger-ui/index.html (debe ser público)..."
curl -I http://localhost:8080/swagger-ui/index.html
echo ""
echo ""

echo "================================"
echo "📊 RESUMEN:"
echo "- Si ves HTTP 200 → ✅ Endpoint funciona"
echo "- Si ves HTTP 403 → ❌ Endpoint bloqueado (necesitas el fix)"
echo "- Si ves HTTP 404 → ⚠️  Endpoint no existe"
echo ""
