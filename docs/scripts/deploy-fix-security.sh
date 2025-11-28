#!/bin/bash

# Script para actualizar SecurityConfig.java en EC2 y recompilar allí

EC2_IP="52.2.172.54"
EC2_USER="ec2-user"

echo "🚀 ACTUALIZANDO SecurityConfig.java EN EC2"
echo "==========================================="
echo ""

# 1. Verificar si tenemos acceso SSH
echo "1️⃣  Verificando acceso SSH a EC2..."
if ! ssh -o ConnectTimeout=5 $EC2_USER@$EC2_IP 'echo "Conexión OK"' > /dev/null 2>&1; then
    echo "❌ No se puede conectar a EC2. Verifica:"
    echo "   - La clave SSH está configurada"
    echo "   - El Security Group permite SSH (puerto 22)"
    echo ""
    echo "Intenta conectar manualmente con:"
    echo "   ssh $EC2_USER@$EC2_IP"
    exit 1
fi
echo "✅ Conexión SSH exitosa"
echo ""

# 2. Copiar el archivo actualizado a EC2
echo "2️⃣  Copiando SecurityConfig.java actualizado a EC2..."
scp src/main/java/cl/huertohogar/huertohogar_api/config/SecurityConfig.java \
    $EC2_USER@$EC2_IP:~/backend-spring-boot-latest/src/main/java/cl/huertohogar/huertohogar_api/config/

if [ $? -eq 0 ]; then
    echo "✅ Archivo copiado exitosamente"
else
    echo "❌ Error al copiar archivo"
    exit 1
fi
echo ""

# 3. Recompilar en EC2
echo "3️⃣  Recompilando proyecto en EC2 (esto puede tomar unos minutos)..."
ssh $EC2_USER@$EC2_IP << 'EOF'
cd ~/backend-spring-boot-latest
echo "Compilando con Maven Wrapper..."
./mvnw clean package -DskipTests
EOF

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
else
    echo "❌ Error en compilación"
    echo ""
    echo "Para ver los logs de compilación, ejecuta:"
    echo "   ssh $EC2_USER@$EC2_IP 'cd ~/backend-spring-boot-latest && ./mvnw clean package -DskipTests'"
    exit 1
fi
echo ""

# 4. Detener aplicación actual
echo "4️⃣  Deteniendo aplicación actual..."
ssh $EC2_USER@$EC2_IP 'pkill -f huertohogar-api'
sleep 3
echo "✅ Aplicación detenida"
echo ""

# 5. Iniciar aplicación con nuevos cambios
echo "5️⃣  Iniciando aplicación con cambios..."
ssh $EC2_USER@$EC2_IP << 'EOF'
cd ~
# Leer variables de entorno si existen
if [ -f ~/.env ]; then
    source ~/.env
fi

nohup java -jar backend-spring-boot-latest/target/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/huertohogar \
  --spring.datasource.username=root \
  --spring.datasource.password=${DB_PASSWORD:-Agu1234567!} \
  --aws.access-key-id=${AWS_ACCESS_KEY_ID} \
  --aws.secret-access-key=TU_AWS_SECRET_KEY \
  --aws.s3.bucket-name=huerto-hogar-documentos \
  --aws.s3.region=us-east-1 \
  > app.log 2>&1 &

echo $! > app.pid
echo "PID: $(cat app.pid)"
EOF

if [ $? -eq 0 ]; then
    echo "✅ Aplicación iniciada"
else
    echo "❌ Error al iniciar aplicación"
    exit 1
fi
echo ""

# 6. Esperar a que la aplicación esté lista
echo "6️⃣  Esperando que la aplicación esté lista (15 segundos)..."
for i in {1..15}; do
    echo -n "."
    sleep 1
done
echo ""
echo ""

# 7. Verificar que esté funcionando
echo "7️⃣  Verificando endpoints..."
echo ""

echo "📍 Probando /hello (debería devolver 'OK'):"
hello_response=$(curl -s http://$EC2_IP:8080/hello)
echo "   Respuesta: $hello_response"
if [ "$hello_response" == "OK" ]; then
    echo "   ✅ Endpoint /hello funciona"
else
    echo "   ⚠️  Respuesta inesperada"
fi
echo ""

echo "📍 Probando /api/v1/products (debería listar productos):"
products_response=$(curl -s -o /dev/null -w "%{http_code}" http://$EC2_IP:8080/api/v1/products)
echo "   HTTP Status: $products_response"
if [ "$products_response" == "200" ]; then
    echo "   ✅ Endpoint /api/v1/products funciona"
    curl -s http://$EC2_IP:8080/api/v1/products | head -c 300
    echo "..."
else
    echo "   ⚠️  Código HTTP: $products_response"
fi
echo ""

echo "📍 Probando /actuator/health:"
health_response=$(curl -s http://$EC2_IP:8080/actuator/health)
echo "   Respuesta: $health_response"
echo ""

echo "==========================================="
echo "✅ ACTUALIZACIÓN COMPLETADA"
echo "==========================================="
echo ""
echo "URLs para probar desde tu frontend:"
echo "- Health: http://$EC2_IP:8080/hello"
echo "- Productos: http://$EC2_IP:8080/api/v1/products"
echo "- Auth: http://$EC2_IP:8080/api/v1/auth/login"
echo "- Swagger: http://$EC2_IP:8080/swagger-ui/index.html"
echo ""
echo "Para ver logs en tiempo real:"
echo "   ssh $EC2_USER@$EC2_IP 'tail -f ~/app.log'"
echo ""
