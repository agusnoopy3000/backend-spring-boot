# Comandos Rápidos para Verificar Spring Boot en EC2

## 🔍 VERIFICACIÓN RÁPIDA (Ejecutar desde tu Mac)

```bash
# 1. Verificar si el servidor responde
curl -v http://52.2.172.54:8080/hello

# 2. Verificar productos
curl -v http://52.2.172.54:8080/api/v1/products

# 3. Verificar health
curl http://52.2.172.54:8080/actuator/health

# 4. Verificar Swagger UI
curl -I http://52.2.172.54:8080/swagger-ui/index.html
```

---

## 🔧 COMANDOS PARA EJECUTAR EN EC2

### Conectarse a EC2
```bash
ssh ec2-user@52.2.172.54
# O usa AWS Console → EC2 → Connect → Session Manager
```

### Ver si Java está corriendo
```bash
ps aux | grep java | grep huertohogar
```

### Ver puerto 8080
```bash
sudo netstat -tlnp | grep 8080
```

### Ver logs recientes
```bash
tail -50 ~/app.log
```

### Ver logs en tiempo real
```bash
tail -f ~/app.log
```

### Buscar errores en logs
```bash
grep -i "error" ~/app.log | tail -20
```

### Ver proceso y PID
```bash
cat ~/app.pid 2>/dev/null || echo "No PID file found"
ps -p $(cat ~/app.pid) 2>/dev/null || echo "Process not running"
```

---

## 🛠️ COMANDOS PARA REINICIAR LA APLICACIÓN

### Detener la aplicación
```bash
pkill -f huertohogar-api
# Esperar un momento
sleep 3
# Verificar que se detuvo
ps aux | grep java | grep huertohogar
```

### Iniciar la aplicación
```bash
cd ~
nohup java -jar backend-spring-boot-latest/target/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/huertohogar \
  --spring.datasource.username=root \
  --spring.datasource.password=TU_PASSWORD \
  --aws.access-key-id=TU_AWS_KEY \
  --aws.secret-access-key=TU_AWS_SECRET_KEY \
  --aws.s3.bucket-name=huerto-hogar-documentos \
  --aws.s3.region=us-east-1 \
  > app.log 2>&1 &

echo $! > app.pid
echo "Aplicación iniciada con PID: $(cat app.pid)"
```

### Verificar que inició correctamente
```bash
# Esperar 10 segundos
sleep 10

# Ver logs
tail -30 ~/app.log

# Probar endpoint
curl http://localhost:8080/hello
```

---

## 🔄 RECOMPILAR PROYECTO EN EC2

```bash
cd ~/backend-spring-boot-latest

# Recompilar
./mvnw clean package -DskipTests

# Ver resultado
ls -lh target/huertohogar-api-*.jar
```

---

## 🩺 VERIFICAR BASE DE DATOS

```bash
# Ver estado de MySQL
sudo systemctl status mysql

# Conectar a MySQL
mysql -u root -p

# Una vez dentro de MySQL:
SHOW DATABASES;
USE huertohogar;
SHOW TABLES;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM products;
SELECT COUNT(*) FROM orders;
EXIT;
```

---

## 🔐 VERIFICAR SECURITY GROUP (AWS Console)

1. Ve a: AWS Console → EC2 → Security Groups
2. Busca el Security Group de tu instancia
3. Verifica Inbound Rules:
   - ✅ SSH (22) desde tu IP
   - ✅ HTTP Custom (8080) desde 0.0.0.0/0
   - ✅ HTTPS (443) si usas SSL
   - ✅ MySQL (3306) desde la VPC (opcional)

---

## 🧪 PRUEBAS DE ENDPOINTS DESDE EC2

```bash
# Health check
curl http://localhost:8080/hello

# Listar productos
curl http://localhost:8080/api/v1/products

# Ver detalle de producto (reemplaza {id})
curl http://localhost:8080/api/v1/products/1

# Health actuator
curl http://localhost:8080/actuator/health

# Registrar usuario nuevo
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test",
    "apellido": "User",
    "email": "test@example.com",
    "password": "password123",
    "run": "12345678-9",
    "telefono": "+56912345678",
    "direccion": "Test Address 123"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

## 📊 INFORMACIÓN DEL SISTEMA

```bash
# Ver uso de memoria
free -h

# Ver uso de disco
df -h

# Ver procesos Java
jps -l

# Ver versión de Java
java -version

# Ver variables de entorno
env | grep -i java
env | grep -i aws
```

---

## 🚨 TROUBLESHOOTING RÁPIDO

### Si el puerto 8080 no responde:
```bash
# Ver qué proceso usa el puerto
sudo lsof -i :8080

# Matar proceso específico
kill -9 <PID>
```

### Si MySQL no conecta:
```bash
# Ver estado
sudo systemctl status mysql

# Reiniciar MySQL
sudo systemctl restart mysql

# Ver logs de MySQL
sudo tail -50 /var/log/mysql/error.log
```

### Si hay problemas de permisos:
```bash
# Dar permisos al JAR
chmod +x ~/backend-spring-boot-latest/target/*.jar

# Dar permisos al directorio de logs
chmod 755 ~/
chmod 644 ~/app.log
```

---

## 📝 NOTAS IMPORTANTES

1. **Password de MySQL**: Reemplaza `TU_PASSWORD` con tu password real
2. **AWS Keys**: Reemplaza `TU_AWS_KEY` y `TU_AWS_SECRET` con tus credenciales
3. **PID File**: El archivo `~/app.pid` contiene el ID del proceso de la aplicación
4. **Logs**: Los logs se guardan en `~/app.log`
5. **JAR**: El archivo compilado está en `~/backend-spring-boot-latest/target/`

---

## 🎯 ESTADO ACTUAL

- ✅ Servidor: **Corriendo en 52.2.172.54:8080**
- ❌ Problema: **403 Forbidden en algunos endpoints**
- 🔧 Solución: **Actualizar SecurityConfig.java** (ver SOLUCION_EC2.md)

---

## 📞 SIGUIENTE PASO

Aplica el fix del `SecurityConfig.java` siguiendo las instrucciones en **SOLUCION_EC2.md**
