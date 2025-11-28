#!/bin/bash

# Script de verificación rápida para antes de la presentación
# Ejecutar 5 minutos antes de presentar

EC2_IP="52.2.172.54"

echo "🎯 VERIFICACIÓN RÁPIDA PRE-PRESENTACIÓN"
echo "========================================"
echo ""
echo "⏰ Ejecutando verificación a las $(date '+%H:%M:%S')"
echo ""

# Contador de errores
errors=0

# 1. Health Check
echo "1️⃣  Health Check..."
response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 http://$EC2_IP:8080/hello)
if [ "$response" = "200" ]; then
    echo "   ✅ Backend responde (HTTP 200)"
else
    echo "   ❌ Backend NO responde (HTTP $response)"
    errors=$((errors+1))
fi

# 2. Productos
echo ""
echo "2️⃣  Endpoint de Productos..."
response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 http://$EC2_IP:8080/api/v1/products)
if [ "$response" = "200" ]; then
    echo "   ✅ Productos accesible (HTTP 200)"
    product_count=$(curl -s http://$EC2_IP:8080/api/v1/products | grep -o '"id":' | wc -l | tr -d ' ')
    echo "   📦 Productos en catálogo: $product_count"
else
    echo "   ❌ Productos NO accesible (HTTP $response)"
    errors=$((errors+1))
fi

# 3. Swagger
echo ""
echo "3️⃣  Swagger UI..."
response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 http://$EC2_IP:8080/swagger-ui/index.html)
if [ "$response" = "200" ]; then
    echo "   ✅ Swagger UI accesible (HTTP 200)"
else
    echo "   ❌ Swagger UI NO accesible (HTTP $response)"
    errors=$((errors+1))
fi

# 4. Login (probar que funciona)
echo ""
echo "4️⃣  Endpoint de Login..."
login_response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 \
  -X POST http://$EC2_IP:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@huertohogar.cl","password":"admin123"}')

if [ "$login_response" = "200" ]; then
    echo "   ✅ Login funciona (HTTP 200)"
    echo "   🔑 Credenciales de admin funcionando"
else
    echo "   ❌ Login NO funciona (HTTP $login_response)"
    errors=$((errors+1))
fi

# 5. Frontend
echo ""
echo "5️⃣  Frontend en S3..."
frontend_response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 \
  http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com)

if [ "$frontend_response" = "200" ]; then
    echo "   ✅ Frontend accesible (HTTP 200)"
else
    echo "   ⚠️  Frontend responde (HTTP $frontend_response)"
fi

# Resumen
echo ""
echo "========================================"
echo "📊 RESUMEN"
echo "========================================"
echo ""

if [ $errors -eq 0 ]; then
    echo "🎉 ¡TODO FUNCIONANDO CORRECTAMENTE!"
    echo ""
    echo "✅ Backend: http://$EC2_IP:8080"
    echo "✅ Swagger: http://$EC2_IP:8080/swagger-ui/index.html"
    echo "✅ Frontend: http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com"
    echo ""
    echo "🚀 ¡LISTO PARA PRESENTAR!"
else
    echo "⚠️  SE ENCONTRARON $errors PROBLEMAS"
    echo ""
    echo "🔧 EJECUTA ESTOS COMANDOS:"
    echo ""
    echo "# Conectar a EC2 y verificar"
    echo "ssh -i ~/Downloads/app-huerto.pem ec2-user@$EC2_IP"
    echo ""
    echo "# Ver proceso Java"
    echo "ps aux | grep java | grep huertohogar"
    echo ""
    echo "# Ver logs"
    echo "tail -50 ~/app.log"
    echo ""
    echo "# Reiniciar si es necesario (ver GUIA_PRESENTACION.md)"
fi

echo ""
echo "========================================"
echo ""

# Abrir URLs en el navegador si todo está bien
if [ $errors -eq 0 ]; then
    echo "¿Abrir URLs en el navegador? (y/n)"
    read -t 5 -n 1 answer
    echo ""
    if [ "$answer" = "y" ]; then
        echo "Abriendo URLs..."
        open "http://$EC2_IP:8080/swagger-ui/index.html"
        open "http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com"
    fi
fi

exit $errors
