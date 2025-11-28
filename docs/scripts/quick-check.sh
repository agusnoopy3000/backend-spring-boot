#!/bin/bash

# Script simplificado para verificar Spring Boot en EC2
EC2_IP="52.2.172.54"

echo "🔍 VERIFICACIÓN RÁPIDA DE SPRING BOOT"
echo "======================================"
echo ""

# 1. Verificar Health Endpoint
echo "1️⃣  Verificando Health Endpoint..."
echo "URL: http://$EC2_IP:8080/actuator/health"
curl -v http://$EC2_IP:8080/actuator/health 2>&1 | head -20
echo ""
echo ""

# 2. Verificar endpoint Hello
echo "2️⃣  Verificando endpoint /hello..."
echo "URL: http://$EC2_IP:8080/hello"
curl -v http://$EC2_IP:8080/hello 2>&1 | head -20
echo ""
echo ""

# 3. Verificar endpoint de productos (público)
echo "3️⃣  Verificando endpoint /products..."
echo "URL: http://$EC2_IP:8080/products"
curl -v http://$EC2_IP:8080/products 2>&1 | head -30
echo ""
echo ""

# 4. Verificar Swagger
echo "4️⃣  Verificando Swagger UI..."
echo "URL: http://$EC2_IP:8080/swagger-ui/index.html"
http_code=$(curl -s -o /dev/null -w "%{http_code}" http://$EC2_IP:8080/swagger-ui/index.html 2>/dev/null)
echo "HTTP Status Code: $http_code"

if [ "$http_code" = "200" ]; then
    echo "✅ Swagger accesible"
elif [ "$http_code" = "000" ]; then
    echo "❌ No se puede conectar al servidor (timeout o conexión rechazada)"
else
    echo "⚠️  Código HTTP: $http_code"
fi
echo ""

echo "======================================"
echo "📋 DIAGNÓSTICO"
echo "======================================"
echo ""

if [ "$http_code" = "000" ] || [ -z "$http_code" ]; then
    echo "❌ EL SERVIDOR NO RESPONDE"
    echo ""
    echo "Posibles causas:"
    echo "1. La aplicación Spring Boot NO está corriendo"
    echo "2. El puerto 8080 está bloqueado por Security Group"
    echo "3. Firewall en EC2 bloqueando conexiones"
    echo "4. La instancia EC2 está apagada"
    echo ""
    echo "Próximos pasos:"
    echo "- Conectarse a EC2 por SSH"
    echo "- Verificar si el proceso Java está corriendo"
    echo "- Revisar los logs de la aplicación"
elif [ "$http_code" = "200" ]; then
    echo "✅ APLICACIÓN FUNCIONANDO CORRECTAMENTE"
    echo ""
    echo "URLs disponibles:"
    echo "- API Base: http://$EC2_IP:8080"
    echo "- Swagger UI: http://$EC2_IP:8080/swagger-ui/index.html"
    echo "- Health Check: http://$EC2_IP:8080/actuator/health"
else
    echo "⚠️  RESPUESTA INESPERADA (HTTP $http_code)"
    echo "Revisa los logs para más detalles"
fi

echo ""
