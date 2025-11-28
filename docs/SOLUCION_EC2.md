# Guía para Verificar y Solucionar el Problema del Backend en EC2

## 📊 DIAGNÓSTICO ACTUAL

✅ **La aplicación Spring Boot SÍ está corriendo en EC2**
❌ **PROBLEMA IDENTIFICADO**: Todos los endpoints devuelven 403 Forbidden

### Causa del Problema

La configuración de seguridad en `SecurityConfig.java` solo permite las rutas con prefijo `/api/v1/`, pero algunos endpoints (como `/hello`, `/products`) no tienen ese prefijo, causando que sean bloqueados.

## 🔧 SOLUCIÓN

He actualizado el archivo `SecurityConfig.java` localmente para permitir ambos prefijos. Ahora necesitas aplicar estos cambios en el servidor EC2.

## 📝 OPCIONES PARA APLICAR LA CORRECCIÓN

### OPCIÓN 1: Usando AWS Console (Más Fácil)

1. **Conectarse a EC2 vía SSH Session Manager**:
   - Ve a AWS Console → EC2 → Instances
   - Selecciona tu instancia (52.2.172.54)
   - Click en "Connect" → "Session Manager"
   - Click en "Connect"

2. **Una vez conectado, ejecuta estos comandos**:

```bash
# Navegar al proyecto
cd ~/backend-spring-boot-latest/src/main/java/cl/huertohogar/huertohogar_api/config/

# Hacer backup del archivo actual
cp SecurityConfig.java SecurityConfig.java.backup

# Editar el archivo
nano SecurityConfig.java
```

3. **Busca esta sección** (alrededor de la línea 39):

```java
.authorizeHttpRequests(authz -> authz
    // Rutas públicas - sin autenticación
    .requestMatchers(
        "/api/v1/auth/**",           // Login y registro
        "/api/v1/products",          // Listar productos (catálogo público)
        "/api/v1/products/**",       // Ver detalle de producto
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api-docs/**",
        "/h2-console/**"
    ).permitAll()
    // Todo lo demás requiere autenticación
    .anyRequest().authenticated()
)
```

4. **Reemplázala por**:

```java
.authorizeHttpRequests(authz -> authz
    // Rutas públicas - sin autenticación
    .requestMatchers(
        "/api/v1/auth/**",           // Login y registro
        "/auth/**",                  // Login y registro (sin prefijo)
        "/api/v1/products",          // Listar productos (catálogo público)
        "/api/v1/products/**",       // Ver detalle de producto
        "/products",                 // Productos (sin prefijo)
        "/products/**",              // Productos (sin prefijo)
        "/hello",                    // Health check
        "/actuator/**",              // Actuator endpoints
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api-docs/**",
        "/h2-console/**"
    ).permitAll()
    // Todo lo demás requiere autenticación
    .anyRequest().authenticated()
)
```

5. **Guardar** (Ctrl+O, Enter, Ctrl+X)

6. **Recompilar el proyecto**:

```bash
cd ~/backend-spring-boot-latest
./mvnw clean package -DskipTests
```

7. **Detener la aplicación actual**:

```bash
pkill -f huertohogar-api
```

8. **Reiniciar la aplicación**:

```bash
cd ~
nohup java -jar backend-spring-boot-latest/target/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/huertohogar \
  --spring.datasource.username=root \
  --spring.datasource.password=TU_PASSWORD_MYSQL \
  --aws.access-key-id=TU_AWS_ACCESS_KEY \
  --aws.secret-access-key=TU_AWS_SECRET_KEY \
  --aws.s3.bucket-name=huerto-hogar-documentos \
  --aws.s3.region=us-east-1 \
  > app.log 2>&1 &
```

9. **Verificar que esté funcionando**:

```bash
# Esperar 10 segundos
sleep 10

# Probar endpoints
curl http://localhost:8080/hello
curl http://localhost:8080/api/v1/products
```

---

### OPCIÓN 2: Usando SSH desde tu Terminal

Si tienes configurado el acceso SSH, ejecuta:

```bash
cd /Users/agustingarridosnoopy/Downloads/backend-spring-boot-latest
./deploy-fix-security.sh
```

---

### OPCIÓN 3: Subir el JAR Compilado (Si puedes compilar localmente)

Si logras compilar localmente:

```bash
# Compilar localmente (en tu Mac)
cd /Users/agustingarridosnoopy/Downloads/backend-spring-boot-latest
./mvnw clean package -DskipTests

# Subir a EC2 (reemplaza con tu método de acceso)
scp target/huertohogar-api-0.0.1-SNAPSHOT.jar ec2-user@52.2.172.54:~/

# Conectar a EC2 y reiniciar
ssh ec2-user@52.2.172.54
pkill -f huertohogar-api
nohup java -jar ~/huertohogar-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
```

---

## 🧪 COMANDOS DE VERIFICACIÓN

### 1. Verificar si el proceso Java está corriendo

```bash
ps aux | grep java | grep huertohogar
```

Deberías ver algo como:
```
ec2-user  1234  java -jar huertohogar-api-0.0.1-SNAPSHOT.jar...
```

### 2. Verificar el puerto 8080

```bash
sudo netstat -tlnp | grep 8080
# O alternativamente:
sudo ss -tlnp | grep 8080
```

Deberías ver:
```
tcp6  0  0 :::8080  :::*  LISTEN  1234/java
```

### 3. Ver los últimos logs

```bash
tail -50 ~/app.log
```

### 4. Ver logs en tiempo real

```bash
tail -f ~/app.log
```

### 5. Probar endpoints desde EC2

```bash
# Health check simple
curl http://localhost:8080/hello

# Listar productos
curl http://localhost:8080/api/v1/products

# Ver health actuator
curl http://localhost:8080/actuator/health
```

### 6. Probar desde tu máquina local

```bash
# Health check
curl http://52.2.172.54:8080/hello

# Productos
curl http://52.2.172.54:8080/api/v1/products

# Swagger UI (abre en navegador)
open http://52.2.172.54:8080/swagger-ui/index.html
```

---

## ❓ DIAGNÓSTICO DE PROBLEMAS COMUNES

### Problema: "Connection refused"

**Causa**: La aplicación no está corriendo

**Solución**:
```bash
# Ver si hay un proceso Java
ps aux | grep java

# Si no hay proceso, iniciar la aplicación
cd ~
nohup java -jar backend-spring-boot-latest/target/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod > app.log 2>&1 &
```

### Problema: "403 Forbidden"

**Causa**: Configuración de seguridad (el problema actual)

**Solución**: Aplicar el fix del SecurityConfig.java (ver arriba)

### Problema: "Connection timeout"

**Causa**: Security Group no permite tráfico en puerto 8080

**Solución**:
1. Ve a AWS Console → EC2 → Security Groups
2. Selecciona el Security Group de tu instancia
3. Inbound Rules → Edit
4. Agregar regla: Type: Custom TCP, Port: 8080, Source: 0.0.0.0/0
5. Save

### Problema: La aplicación se inicia pero falla después

**Causa**: Error de conexión a base de datos

**Solución**:
```bash
# Verificar que MySQL esté corriendo
sudo systemctl status mysql

# Ver errores en logs
grep -i "error" ~/app.log | tail -20

# Verificar conexión a base de datos
mysql -u root -p -e "SHOW DATABASES;"
```

---

## 🎯 ENDPOINTS PARA PROBAR DESDE TU FRONTEND

Una vez aplicada la corrección, estos endpoints deberían funcionar:

### Públicos (sin autenticación):

```
GET  http://52.2.172.54:8080/hello
GET  http://52.2.172.54:8080/api/v1/products
GET  http://52.2.172.54:8080/api/v1/products/{id}
POST http://52.2.172.54:8080/api/v1/auth/register
POST http://52.2.172.54:8080/api/v1/auth/login
```

### Requieren autenticación (token JWT):

```
POST   http://52.2.172.54:8080/api/v1/orders
GET    http://52.2.172.54:8080/api/v1/orders
GET    http://52.2.172.54:8080/api/v1/users/me
```

### Solo ADMIN:

```
POST   http://52.2.172.54:8080/api/v1/products
PUT    http://52.2.172.54:8080/api/v1/products/{id}
DELETE http://52.2.172.54:8080/api/v1/products/{id}
PUT    http://52.2.172.54:8080/api/v1/orders/{id}/status
```

---

## 📋 CONFIGURACIÓN ACTUAL DEL PROYECTO

**Servidor**: 52.2.172.54  
**Puerto**: 8080  
**Base de datos**: MySQL local en EC2  
**Storage**: AWS S3 (huerto-hogar-documentos)  
**Perfil activo**: prod  

---

## 📞 PRÓXIMOS PASOS

1. **Conectarte a EC2** (vía AWS Console Session Manager o SSH)
2. **Aplicar el fix** del SecurityConfig.java
3. **Recompilar** el proyecto
4. **Reiniciar** la aplicación
5. **Verificar** que los endpoints respondan correctamente
6. **Probar desde tu frontend**

---

## 🔍 ARCHIVO ACTUALIZADO

El archivo `SecurityConfig.java` actualizado está en:
```
/Users/agustingarridosnoopy/Downloads/backend-spring-boot-latest/src/main/java/cl/huertohogar/huertohogar_api/config/SecurityConfig.java
```

Puedes copiarlo manualmente a EC2 usando cualquier método que prefieras (SCP, Session Manager con copy-paste, etc.)

---

¿Necesitas ayuda con algún paso específico?
