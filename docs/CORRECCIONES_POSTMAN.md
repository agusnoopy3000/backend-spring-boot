# ✅ CORRECCIONES APLICADAS - COLECCIÓN POSTMAN

## 🐛 Problemas Corregidos

### 1. Error 400 en Registro de Usuario
**Problema:** Campo incorrecto en el JSON
- ❌ **Antes:** `"apellido": "Demo"` (singular)
- ✅ **Ahora:** `"apellidos": "Demo Prueba"` (plural)

**Causa:** El backend en `RegisterRequest.java` espera el campo `apellidos` con validación `@NotBlank`.

### 2. Error de Validación de Password
**Problema:** Password no cumplía requisitos de seguridad
- ❌ **Antes:** `"password": "demo123"` (sin carácter especial)
- ✅ **Ahora:** `"password": "Demo123!"` (con carácter especial)

**Causa:** El backend requiere mínimo 8 caracteres con al menos 1 carácter especial.

---

## 📝 Datos Actualizados en la Colección

### Request: "4. Registro de Usuario"
```json
{
  "email": "test@demo.com",
  "run": "12345678-9",
  "nombre": "Usuario",
  "apellidos": "Demo Prueba",
  "password": "Demo123!",
  "direccion": "Calle Demo 123",
  "telefono": "+56912345678"
}
```

### Validaciones del Backend
✅ **email:** Formato válido de email  
✅ **run:** 8-12 caracteres (RUT chileno)  
✅ **nombre:** 2-50 caracteres  
✅ **apellidos:** 2-50 caracteres  
✅ **password:** Mínimo 8 caracteres + 1 carácter especial  
✅ **telefono:** Formato válido (+56912345678)  
✅ **direccion:** No vacío  

---

## 🧪 Cómo Probar el Registro

### Paso 1: Importar Colección Actualizada
1. Si ya habías importado la colección, **elimínala**
2. Importa nuevamente: `Huerto_Hogar_API_Presentacion.postman_collection.json`

### Paso 2: Probar Registro
1. Abre Postman
2. Selecciona **"4. Registro de Usuario"**
3. Click en **"Send"**
4. ✅ **Resultado esperado:**
   ```json
   Status: 200 OK
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "email": "test@demo.com",
     "nombre": "Usuario",
     "roles": ["ROLE_USER"]
   }
   ```

### Paso 3: Probar Login con el Usuario Registrado
1. Selecciona **"6. Login Usuario Regular"**
2. Modifica el Body:
   ```json
   {
     "email": "test@demo.com",
     "password": "Demo123!"
   }
   ```
3. Click en **"Send"**
4. ✅ Debería devolver un token JWT

---

## 🔑 Credenciales Pre-configuradas

### Administrador
```json
{
  "email": "admin@huertohogar.cl",
  "password": "admin123"
}
```
- ✅ **Request:** "5. Login Admin"
- ✅ **Roles:** ADMIN, USER

### Usuario Regular (Si existe en BD)
```json
{
  "email": "user@huertohogar.cl",
  "password": "user123"
}
```
- ✅ **Request:** "6. Login Usuario Regular"
- ✅ **Roles:** USER

### Usuario Demo (Recién registrado)
```json
{
  "email": "test@demo.com",
  "password": "Demo123!"
}
```
- ✅ **Roles:** USER

---

## 🚨 Problemas Conocidos y Soluciones

### Error 403 Forbidden en Login
**Posibles causas:**
1. ❌ Credenciales incorrectas
2. ❌ Usuario no existe en la base de datos
3. ❌ Password incorrecto

**Solución:**
- Primero registra un usuario con **"4. Registro de Usuario"**
- Luego usa esas credenciales para login

### Error 400 Bad Request
**Posibles causas:**
1. ❌ Campo `apellidos` mal escrito (debe ser plural)
2. ❌ Password sin carácter especial
3. ❌ Email con formato inválido
4. ❌ RUN con formato inválido

**Solución:**
- Verifica que todos los campos cumplan las validaciones
- Usa los ejemplos proporcionados en la colección

### Error 409 Conflict (Usuario ya existe)
**Causa:** El email ya está registrado

**Solución:**
- Usa un email diferente, o
- Usa el login con las credenciales existentes

---

## 📊 Flujo Completo de Prueba

```
1. Health Check (GET /hello)
   ↓
2. Listar Productos (GET /api/v1/products)
   ↓
3. Registrar Usuario (POST /api/v1/auth/register)
   ↓
4. Login (POST /api/v1/auth/login) → Guarda token
   ↓
5. Ver Perfil (GET /api/v1/users/profile) → Usa token
   ↓
6. Ver Pedidos (GET /api/v1/pedidos/mis-pedidos) → Usa token
```

---

## ✅ Checklist Pre-Presentación

- [ ] Importar colección actualizada en Postman
- [ ] Probar Health Check → 200 OK
- [ ] Probar Listar Productos → 200 OK, 24 productos
- [ ] Probar Login Admin → 200 OK, token guardado
- [ ] Probar Registro de Usuario → 200 OK, token devuelto
- [ ] Probar Ver Perfil (con token) → 200 OK
- [ ] Verificar que Swagger está accesible
- [ ] Confirmar que frontend se conecta a backend

---

## 📞 Contacto
Si encuentras algún otro problema, revisa los logs en EC2:
```bash
ssh -i "backend-huertoCasa.pem" ec2-user@52.2.172.54
tail -f ~/app.log
```
