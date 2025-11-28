# 🎯 GUÍA RÁPIDA PARA PRESENTACIÓN - Backend EC2

**Fecha**: 26 de noviembre de 2025  
**Backend URL**: http://52.2.172.54:8080  
**Frontend URL**: http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com

---

## ✅ VERIFICACIÓN RÁPIDA (5 minutos antes de presentar)

### 1️⃣ Verificar que EC2 está corriendo

```bash
# Desde tu Mac - Verificación completa
curl -i http://52.2.172.54:8080/hello
```

**✅ Respuesta esperada:**
```
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 2

OK
```

**❌ Si falla**, ejecuta:
```bash
ssh -i ~/Downloads/app-huerto.pem ec2-user@52.2.172.54 'ps aux | grep java | grep huertohogar'
```

---

### 2️⃣ Verificar endpoints críticos

```bash
# Health Check
curl http://52.2.172.54:8080/hello

# Productos (debe retornar JSON)
curl http://52.2.172.54:8080/api/v1/products

# Swagger (abre en navegador)
open http://52.2.172.54:8080/swagger-ui/index.html
```

---

### 3️⃣ Verificar conectividad Frontend → Backend

```bash
# Probar CORS desde el navegador
# Abre la consola del navegador en tu frontend y ejecuta:
fetch('http://52.2.172.54:8080/api/v1/products')
  .then(res => res.json())
  .then(data => console.log('✅ Backend responde:', data))
  .catch(err => console.error('❌ Error:', err));
```

---

## 🔧 COMANDOS DE EMERGENCIA (si algo falla)

### Si el backend NO responde:

```bash
# 1. Conectar a EC2
ssh -i ~/Downloads/app-huerto.pem ec2-user@52.2.172.54

# 2. Verificar proceso
ps aux | grep java | grep huertohogar

# 3. Ver logs
tail -50 ~/app.log

# 4. Si no está corriendo, reiniciar
pkill -f huertohogar-api
sleep 5
cd ~
nohup java -jar ~/backend-spring-boot/target/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/huertohogar \
  --spring.datasource.username=root \
  --spring.datasource.password=Admin_2024 \
  --aws.access-key-id=TU_AWS_ACCESS_KEY \
  --aws.secret-access-key=TU_AWS_SECRET_KEY \
  --aws.s3.bucket-name=huerto-hogar-documentos \
  --aws.s3.region=us-east-1 \
  > app.log 2>&1 &

# 5. Esperar 15 segundos
sleep 15

# 6. Probar
curl http://localhost:8080/hello
```

---

## 📮 COLECCIÓN POSTMAN - ENDPOINTS CRÍTICOS

### 🟢 **1. Health Check** (Sin autenticación)

```
GET http://52.2.172.54:8080/hello
```

**Respuesta esperada:** `OK` (200)

---

### 🟢 **2. Listar Productos** (Sin autenticación)

```
GET http://52.2.172.54:8080/api/v1/products
```

**Headers:**
```
Content-Type: application/json
```

**Respuesta esperada:** Array de productos (200)

```json
[
  {
    "id": 1,
    "codigo": "VRD-001",
    "nombre": "Lechuga Orgánica",
    "descripcion": "Lechuga fresca cultivada sin pesticidas",
    "precio": 1500.0,
    "stock": 50,
    "imagen": "https://ejemplo.com/lechuga.jpg",
    "categoria": "Verduras"
  }
]
```

---

### 🟢 **3. Obtener Producto por ID** (Sin autenticación)

```
GET http://52.2.172.54:8080/api/v1/products/1
```

**Respuesta esperada:** Producto específico (200)

---

### 🟡 **4. Registro de Usuario** (Sin autenticación)

```
POST http://52.2.172.54:8080/api/v1/auth/register
```

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "test@demo.com",
  "run": "12345678-9",
  "nombre": "Usuario",
  "apellido": "Demo",
  "password": "demo123",
  "direccion": "Calle Demo 123",
  "telefono": "+56912345678"
}
```

**Respuesta esperada:** (201)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "email": "test@demo.com",
    "nombre": "Usuario",
    "apellido": "Demo",
    "rol": "USER"
  }
}
```

---

### 🟡 **5. Login** (Sin autenticación)

```
POST http://52.2.172.54:8080/api/v1/auth/login
```

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "admin@huertohogar.cl",
  "password": "admin123"
}
```

**Respuesta esperada:** (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "email": "admin@huertohogar.cl",
    "nombre": "Admin",
    "apellido": "Huerto Hogar",
    "rol": "ADMIN"
  }
}
```

---

### 🔒 **6. Obtener Mi Perfil** (Requiere autenticación)

```
GET http://52.2.172.54:8080/api/v1/users/me
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_JWT>
```

**Respuesta esperada:** (200)
```json
{
  "email": "admin@huertohogar.cl",
  "run": "11111111-1",
  "nombre": "Admin",
  "apellido": "Huerto Hogar",
  "direccion": "Av Principal 123",
  "telefono": "+56912345678",
  "rol": "ADMIN",
  "createdAt": "2024-11-25T10:00:00"
}
```

---

### 🔒 **7. Crear Pedido** (Requiere autenticación)

```
POST http://52.2.172.54:8080/api/v1/orders
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_JWT>
```

**Body (JSON):**
```json
{
  "items": [
    {
      "productoId": "VRD-001",
      "cantidad": 2
    },
    {
      "productoId": "VRD-002",
      "cantidad": 1
    }
  ],
  "direccionEntrega": "Calle Demo 123, Santiago",
  "region": "Metropolitana",
  "comuna": "Santiago",
  "comentarios": "Dejar en portería",
  "fechaEntrega": "2024-11-30T14:00:00"
}
```

**Respuesta esperada:** (201)
```json
{
  "id": 1,
  "userEmail": "admin@huertohogar.cl",
  "items": [...],
  "total": 4500.0,
  "estado": "PENDIENTE",
  "direccionEntrega": "Calle Demo 123, Santiago",
  "createdAt": "2024-11-26T07:00:00"
}
```

---

### 🔒 **8. Listar Mis Pedidos** (Requiere autenticación)

```
GET http://52.2.172.54:8080/api/v1/orders
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_JWT>
```

**Respuesta esperada:** Array de pedidos del usuario (200)

---

### 🔴 **9. Crear Producto** (Solo ADMIN)

```
POST http://52.2.172.54:8080/api/v1/products
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <TOKEN_ADMIN>
```

**Body (JSON):**
```json
{
  "codigo": "VRD-010",
  "nombre": "Tomate Cherry",
  "descripcion": "Tomates cherry orgánicos",
  "precio": 2500,
  "stock": 30,
  "imagen": "https://ejemplo.com/tomate.jpg",
  "categoria": "Verduras"
}
```

**Respuesta esperada:** (201)

---

### 🔴 **10. Actualizar Estado de Pedido** (Solo ADMIN)

```
PUT http://52.2.172.54:8080/api/v1/orders/1/status?newStatus=CONFIRMADO
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <TOKEN_ADMIN>
```

**Respuesta esperada:** (200)
```json
{
  "id": 1,
  "estado": "CONFIRMADO",
  ...
}
```

---

## 🎨 SWAGGER UI (Documentación Interactiva)

**URL:** http://52.2.172.54:8080/swagger-ui/index.html

Usa Swagger durante la presentación para demostrar:
- ✅ Documentación automática de la API
- ✅ Probar endpoints en vivo
- ✅ Ver esquemas de datos
- ✅ Autenticación con JWT

---

## 📊 ESTADOS DE PEDIDOS (OrderStatus)

Para el flujo de actualización de estados:

```
PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO
                ↓
            CANCELADO
```

---

## 🔑 CREDENCIALES DE PRUEBA

### Usuario Administrador:
```
Email: admin@huertohogar.cl
Password: admin123
Rol: ADMIN
```

### Usuario Regular:
```
Email: usuario@huertohogar.cl
Password: usuario123
Rol: USER
```

---

## 🚨 TROUBLESHOOTING EN VIVO

### Error: "403 Forbidden"
**Causa:** Token JWT expirado o inválido  
**Solución:** Hacer login nuevamente y obtener un nuevo token

### Error: "Connection refused"
**Causa:** Backend no está corriendo  
**Solución:** Ejecutar comandos de emergencia (arriba)

### Error: "CORS policy"
**Causa:** Frontend no está en los orígenes permitidos  
**Solución:** Ya está configurado para tu S3, no debería fallar

---

## 📱 DEMOSTRACIÓN SUGERIDA (Orden)

1. **Health Check** - Mostrar que el backend está vivo
2. **Listar Productos** - Catálogo público
3. **Registro** - Crear un nuevo usuario
4. **Login** - Obtener token JWT
5. **Mi Perfil** - Endpoint autenticado
6. **Crear Pedido** - Flujo completo
7. **Listar Pedidos** - Ver pedidos del usuario
8. **(ADMIN) Actualizar Estado** - Cambiar pedido a CONFIRMADO
9. **Swagger UI** - Mostrar documentación

---

## ✅ CHECKLIST PRE-PRESENTACIÓN

- [ ] Backend responde en http://52.2.172.54:8080/hello
- [ ] Frontend carga en http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com
- [ ] Login funciona desde el frontend
- [ ] Se pueden listar productos
- [ ] Se pueden crear pedidos
- [ ] Swagger UI es accesible
- [ ] Base de datos tiene datos de ejemplo

---

## 🎯 PUNTOS CLAVE PARA DESTACAR

1. ✅ **Arquitectura REST** completa con Spring Boot
2. ✅ **Seguridad JWT** con roles (USER/ADMIN)
3. ✅ **Base de datos MySQL** en EC2
4. ✅ **Documentación Swagger** automática
5. ✅ **CORS configurado** para frontend en S3
6. ✅ **Gestión de estados** de pedidos
7. ✅ **Validaciones** con Bean Validation
8. ✅ **Migraciones Flyway** para versionado de BD

---

## 📞 CONTACTO DE EMERGENCIA

Si algo falla durante la presentación:

1. Mantén la calma 😊
2. Abre Swagger UI (siempre funciona)
3. Usa los comandos de verificación
4. Explica la arquitectura mientras se reinicia

---

**¡MUCHA SUERTE EN TU PRESENTACIÓN! 🚀🎉**

---

**Última actualización:** 26 de noviembre de 2025, 04:10 AM  
**Estado:** ✅ Backend funcionando correctamente
