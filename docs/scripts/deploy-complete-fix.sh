#!/bin/bash

# Script completo para desplegar el fix de SecurityConfig en EC2
# Uso: ./deploy-complete-fix.sh

set -e

EC2_IP="52.2.172.54"
EC2_USER="ec2-user"
PEM_KEY="$HOME/Downloads/app-huerto.pem"
LOCAL_FILE="src/main/java/cl/huertohogar/huertohogar_api/config/SecurityConfig.java"
REMOTE_PATH="~/backend-spring-boot-latest/src/main/java/cl/huertohogar/huertohogar_api/config/"

echo "🚀 DESPLEGANDO FIX DE SECURITYCONFIG A EC2"
echo "============================================"
echo ""

# Verificar que existe el archivo local
if [ ! -f "$LOCAL_FILE" ]; then
    echo "❌ Error: No se encuentra el archivo $LOCAL_FILE"
    exit 1
fi

# Verificar que existe la clave PEM
if [ ! -f "$PEM_KEY" ]; then
    echo "❌ Error: No se encuentra la clave PEM en $PEM_KEY"
    exit 1
fi

echo "✅ Archivo local encontrado"
echo "✅ Clave PEM encontrada"
echo ""

# 1. Subir el archivo actualizado
echo "📤 Paso 1: Subiendo SecurityConfig.java actualizado a EC2..."
scp -i "$PEM_KEY" "$LOCAL_FILE" "$EC2_USER@$EC2_IP:$REMOTE_PATH"

if [ $? -eq 0 ]; then
    echo "✅ Archivo subido correctamente"
else
    echo "❌ Error al subir el archivo"
    exit 1
fi
echo ""

# 2. Conectar a EC2 y ejecutar los comandos de recompilación y reinicio
echo "🔧 Paso 2: Recompilando y reiniciando aplicación en EC2..."
echo ""

ssh -i "$PEM_KEY" "$EC2_USER@$EC2_IP" << 'ENDSSH'
set -e

echo "📁 Navegando al directorio del proyecto..."
cd ~/backend-spring-boot-latest

echo "🔨 Recompilando proyecto (esto tomará unos minutos)..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
else
    echo "❌ Error en la compilación"
    exit 1
fi

echo ""
echo "🛑 Deteniendo aplicación actual..."
pkill -f huertohogar-api || echo "No había proceso previo corriendo"
sleep 5

echo ""
echo "🚀 Iniciando aplicación con nueva configuración..."
cd ~
nohup java -jar backend-spring-boot-latest/target/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/huertohogar \
  --spring.datasource.username=root \
  --spring.datasource.password=Admin_2024 \
  --aws.access-key-id=TU_AWS_ACCESS_KEY \
  --aws.secret-access-key=TU_AWS_SECRET_KEY \
  --aws.s3.bucket-name=huerto-hogar-documentos \
  --aws.s3.region=us-east-1 \
  > app.log 2>&1 &

echo "⏳ Esperando 15 segundos para que la aplicación inicie..."
sleep 15

echo ""
echo "🧪 Probando endpoints..."
echo ""

echo "Test 1: /hello"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/hello

echo "Test 2: /api/v1/products"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/api/v1/products

echo "Test 3: /actuator/health"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/actuator/health

echo ""
echo "✅ Proceso completado"
echo ""
echo "📋 Verificar logs con: tail -f ~/app.log"
echo "🔍 Ver proceso con: ps aux | grep java"

ENDSSH

echo ""
echo "============================================"
echo "✅ DESPLIEGUE COMPLETADO"
echo "============================================"
echo ""
echo "🌐 URLs para probar desde tu navegador:"
echo "  - Swagger: http://$EC2_IP:8080/swagger-ui/index.html"
echo "  - Health: http://$EC2_IP:8080/hello"
echo "  - Products: http://$EC2_IP:8080/api/v1/products"
echo ""
echo "🎯 Endpoints configurados en tu frontend:"
echo "  - Base URL: http://$EC2_IP:8080"
echo ""
