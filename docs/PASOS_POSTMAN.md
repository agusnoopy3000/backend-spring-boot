# 📮 GUÍA PASO A PASO - POSTMAN

## 🚀 CONFIGURACIÓN INICIAL

### Paso 1: Importar la Colección
1. Abre Postman
2. Click en **"Import"** (esquina superior izquierda)
3. Arrastra el archivo `Huerto_Hogar_API_Presentacion.postman_collection.json`
4. Click en **"Import"**
5. Verás la colección con 17 endpoints organizados por categorías

### Paso 2: Crear un Environment (Opcional pero recomendado)
1. Click en el ícono de ⚙️ (Settings) arriba a la derecha
2. Click en **"Environments"** > **"Create Environment"**
3. Nombre: `Huerto Hogar - EC2`
4. Agrega estas variables:
   - `base_url`: `http://52.2.172.54:8080`
   - `jwt_token`: (déjalo vacío, se llenará automáticamente)
5. Click en **"Save"**
6. Selecciona el environment en el dropdown superior derecho

---

## 📋 SECUENCIA DE PRUEBA RECOMENDADA

### 🟢 FASE 1: Endpoints Públicos (Sin Autenticación)

#### ✅ Test 1: Health Check
**Endpoint:** `GET /hello`
1. Abre el request **"1. Health Check"**
2. Click en **"Send"**
3. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: "¡Hola desde Spring Boot! La API está funcionando correctamente."
   ```

#### ✅ Test 2: Listar Productos
**Endpoint:** `GET /api/v1/products`
1. Abre el request **"2. Listar Productos"**
2. Click en **"Send"**
3. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Array con 24 productos
   ```
4. **Copia el ID de un producto** para usarlo en el siguiente test

#### ✅ Test 3: Obtener Producto por ID
**Endpoint:** `GET /api/v1/products/1`
1. Abre el request **"3. Obtener Producto por ID"**
2. Si quieres probar con otro ID, cambia el `1` al final de la URL
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Detalles del producto con ID 1
   ```

---

### 🟡 FASE 2: Autenticación

#### 🔑 Test 4: Login Admin (IMPORTANTE)
**Endpoint:** `POST /api/v1/auth/login`
1. Abre el request **"5. Login Admin"**
2. Verifica el Body:
   ```json
   {
     "email": "admin@huertohogar.cl",
     "password": "admin123"
   }
   ```
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "email": "admin@huertohogar.cl",
     "nombre": "Administrador",
     "roles": ["ROLE_ADMIN"]
   }
   ```
5. 🎯 **¡IMPORTANTE!** El token se guardará automáticamente en la variable `{{jwt_token}}`
6. Verifica en la pestaña **"Console"** (abajo) que dice: `"Token guardado: eyJ..."`

#### 📝 Test 5: Registro de Usuario (Opcional)
**Endpoint:** `POST /api/v1/auth/register`
1. Abre el request **"4. Registro de Usuario"**
2. Puedes modificar el Body con tus datos:
   ```json
   {
     "email": "miusuario@demo.com",
     "run": "98765432-1",
     "nombre": "Tu Nombre",
     "apellidos": "Tu Apellido Completo",
     "password": "MiPassword123!",
     "direccion": "Mi Dirección 456",
     "telefono": "+56987654321"
   }
   ```
   
   ⚠️ **NOTA:** El password debe tener:
   - Mínimo 8 caracteres
   - Al menos 1 carácter especial (!@#$%^&*)
   ```
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Usuario registrado con token
   ```

---

### 🔒 FASE 3: Endpoints Autenticados (Requiere JWT)

> ⚠️ **IMPORTANTE:** Antes de continuar, asegúrate de haber hecho login (Test 4)

#### 👤 Test 6: Mi Perfil
**Endpoint:** `GET /api/v1/users/me`
1. Abre el request **"7. Mi Perfil"**
2. Verifica que en **Headers** está:
   ```
   Authorization: Bearer {{jwt_token}}
   ```
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Datos del usuario administrador
   ```

#### 🛒 Test 7: Crear Pedido
**Endpoint:** `POST /api/v1/orders`
1. Abre el request **"8. Crear Pedido"**
2. Revisa el Body (puedes modificar las cantidades):
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
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Pedido creado con ID, estado PENDIENTE y total calculado
   ```
5. **¡Anota el ID del pedido!** Lo usarás en los siguientes tests

#### 📦 Test 8: Listar Mis Pedidos
**Endpoint:** `GET /api/v1/orders`
1. Abre el request **"9. Listar Mis Pedidos"**
2. Click en **"Send"**
3. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Lista de todos tus pedidos
   ```

#### 📋 Test 9: Obtener Pedido por ID
**Endpoint:** `GET /api/v1/orders/1`
1. Abre el request **"10. Obtener Pedido por ID"**
2. Cambia el `1` por el ID del pedido que creaste
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Detalles completos del pedido
   ```

---

### 🔴 FASE 4: Endpoints de Administrador

> ⚠️ **IMPORTANTE:** Asegúrate de estar logueado como ADMIN (Test 4)

#### ➕ Test 10: Crear Producto
**Endpoint:** `POST /api/v1/products`
1. Abre el request **"11. Crear Producto"**
2. Revisa el Body:
   ```json
   {
     "codigo": "VRD-099",
     "nombre": "Producto Demo",
     "descripcion": "Producto creado en la presentación",
     "precio": 2500,
     "stock": 30,
     "imagen": "https://ejemplo.com/producto.jpg",
     "categoria": "Verduras"
   }
   ```
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Producto creado con ID asignado
   ```
5. **Anota el ID** para usarlo en los siguientes tests

#### ✏️ Test 11: Actualizar Producto
**Endpoint:** `PUT /api/v1/products/1`
1. Abre el request **"12. Actualizar Producto"**
2. Cambia el `1` por el ID del producto que creaste
3. Modifica el Body como quieras
4. Click en **"Send"**
5. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Producto actualizado
   ```

#### 🗑️ Test 12: Eliminar Producto
**Endpoint:** `DELETE /api/v1/products/99`
1. Abre el request **"13. Eliminar Producto"**
2. Cambia el `99` por el ID del producto que creaste (para no eliminar datos importantes)
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 204 No Content
   ```

#### 📊 Test 13-15: Actualizar Estados de Pedido
**Secuencia:** PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO

**Test 13: Estado CONFIRMADO**
1. Abre el request **"14. Actualizar Estado - CONFIRMADO"**
2. Cambia el ID del pedido en la URL (`/orders/1/status`)
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Pedido con estado actualizado a CONFIRMADO
   ```

**Test 14: Estado ENVIADO**
1. Abre el request **"15. Actualizar Estado - ENVIADO"**
2. Cambia el ID del pedido
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Pedido con estado actualizado a ENVIADO
   ```

**Test 15: Estado ENTREGADO**
1. Abre el request **"16. Actualizar Estado - ENTREGADO"**
2. Cambia el ID del pedido
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Pedido con estado actualizado a ENTREGADO
   ```

#### 👥 Test 16: Listar Todos los Usuarios
**Endpoint:** `GET /api/v1/users`
1. Abre el request **"17. Listar Todos los Usuarios"**
2. Click en **"Send"**
3. ✅ **Resultado esperado:**
   ```
   Status: 200 OK
   Body: Lista de todos los usuarios del sistema
   ```

---

## 🎯 FLUJO COMPLETO PARA PRESENTACIÓN (5 minutos)

### Demostración Rápida:
1. ✅ **Health Check** → Mostrar que el backend está vivo
2. ✅ **Listar Productos** → Mostrar catálogo de 24 productos
3. 🔑 **Login Admin** → Autenticarse como administrador
4. 🛒 **Crear Pedido** → Hacer un pedido de 2-3 productos
5. 📊 **Actualizar Estado** → Cambiar de PENDIENTE a CONFIRMADO
6. ➕ **Crear Producto** → Crear un producto en vivo
7. 👥 **Listar Usuarios** → Mostrar gestión de usuarios

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### ❌ Error: 403 Forbidden
**Causa:** No estás autenticado o el token expiró
**Solución:**
1. Ve al request **"5. Login Admin"**
2. Haz login nuevamente
3. Verifica que el token se guardó en el Console
4. Intenta el request nuevamente

### ❌ Error: 401 Unauthorized
**Causa:** Token inválido o rol insuficiente
**Solución:**
1. Para endpoints de ADMIN, asegúrate de usar el login de admin
2. Verifica que el header Authorization tenga el formato: `Bearer {{jwt_token}}`

### ❌ Error: 404 Not Found
**Causa:** El recurso no existe (ej: producto o pedido con ID inexistente)
**Solución:**
1. Verifica que el ID existe
2. Usa IDs de recursos que hayas creado o que sepas que existen

### ❌ Error: 400 Bad Request
**Causa:** Datos inválidos en el Body
**Solución:**
1. Verifica el formato JSON
2. Asegúrate de que todos los campos requeridos estén presentes
3. Revisa que los tipos de datos sean correctos (números, strings, fechas)

### ❌ Error: Connection Refused
**Causa:** El backend no está corriendo
**Solución:**
1. Verifica que el servidor EC2 esté activo: `http://52.2.172.54:8080/hello`
2. Contacta al equipo de infraestructura

---

## 📊 CHECKLIST DE VALIDACIÓN

Antes de la presentación, verifica que funcionen:

- [ ] ✅ Health Check (GET /hello)
- [ ] ✅ Listar Productos (GET /api/v1/products)
- [ ] 🔑 Login Admin (POST /api/v1/auth/login)
- [ ] 👤 Mi Perfil (GET /api/v1/users/me)
- [ ] 🛒 Crear Pedido (POST /api/v1/orders)
- [ ] 📦 Listar Pedidos (GET /api/v1/orders)
- [ ] ➕ Crear Producto (POST /api/v1/products)
- [ ] 📊 Actualizar Estado (PUT /api/v1/orders/{id}/status)

---

## 🎓 TIPS PARA LA PRESENTACIÓN

1. **Prepara datos de prueba:** Antes de la presentación, crea 1-2 productos y pedidos de prueba
2. **Mantén el token válido:** Haz login justo antes de empezar
3. **Usa IDs reales:** Anota los IDs de productos y pedidos que existen
4. **Ten un plan B:** Si algo falla, ten capturas de pantalla de respuestas exitosas
5. **Explica los códigos HTTP:** 200 OK, 201 Created, 401 Unauthorized, 403 Forbidden
6. **Muestra el Authorization Header:** Explica cómo funciona JWT
7. **Demuestra la seguridad:** Intenta acceder a un endpoint de ADMIN sin autenticación

---

## 📞 CONTACTO DE EMERGENCIA

Si algo falla durante la presentación:
- **Backend URL:** http://52.2.172.54:8080
- **Swagger UI:** http://52.2.172.54:8080/swagger-ui/index.html
- **Frontend:** http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com
- **Script de verificación:** `./verificar-antes-presentar.sh`

---

**¡Éxito en tu presentación! 🚀🌱**
