# Configuración de Firebase para Backend Spring Boot
## Huerto Hogar - Autenticación Híbrida

---

## 📋 Resumen de Implementación

Se ha implementado autenticación híbrida Firebase + JWT en el backend:

### Archivos Creados/Modificados:

| Archivo | Descripción |
|---------|-------------|
| `pom.xml` | Añadida dependencia `firebase-admin:9.2.0` |
| `FirebaseConfig.java` | Configuración e inicialización de Firebase Admin SDK |
| `FirebaseSyncRequest.java` | DTO para el endpoint de sincronización |
| `FirebaseAuthService.java` | Servicio que valida tokens de Firebase y genera JWT |
| `AuthController.java` | Añadido endpoint `/auth/firebase-sync` |
| `User.java` | Añadido campo `firebaseUid` |
| `UserRepository.java` | Añadido método `findByFirebaseUid()` |
| `V5__Add_firebase_uid_column.sql` | Migración Flyway para nueva columna |
| `application.properties` | Propiedades de configuración Firebase |

---

## 🔧 Configuración en EC2

### Paso 1: Obtener credenciales de Firebase

1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Seleccionar proyecto: `huerto-hogar-cbe8d`
3. Ir a **Configuración del proyecto** (⚙️)
4. Pestaña **Cuentas de servicio**
5. Clic en **Generar nueva clave privada**
6. Descargar el archivo JSON

### Paso 2: Configurar en EC2

**Opción A: Variable de entorno (RECOMENDADO)**

```bash
# Copiar el contenido del JSON a una variable de entorno
ssh -i tu-key.pem ec2-user@52.2.172.54

# Editar el archivo de inicio o crear un script
export FIREBASE_CREDENTIALS_JSON='{"type":"service_account","project_id":"huerto-hogar-cbe8d",...}'
```

**Opción B: Archivo en el servidor**

```bash
# Subir el archivo al servidor
scp -i tu-key.pem firebase-service-account.json ec2-user@52.2.172.54:~/

# Mover a ubicación del JAR
mv firebase-service-account.json ~/config/
```

### Paso 3: Iniciar el backend con Firebase habilitado

```bash
# Con variable de entorno
nohup java -jar huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --firebase.enabled=true \
  > app.log 2>&1 &

# O con archivo
nohup java -jar huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --firebase.enabled=true \
  --firebase.credentials.path=/home/ec2-user/config/firebase-service-account.json \
  > app.log 2>&1 &
```

---

## 📡 Endpoint Disponible

### POST /api/v1/auth/firebase-sync

**Request:**
```json
{
  "firebaseIdToken": "eyJhbGciOiJSUzI1NiIs...",
  "run": "19.011.022-K",
  "nombre": "Juan",
  "apellidos": "Pérez González",
  "direccion": "Av. Principal 123",
  "telefono": "+56912345678"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "email": "usuario@email.com",
    "nombre": "Juan",
    "apellido": "Pérez González",
    "run": "19.011.022-K",
    "rol": "USER",
    "createdAt": "2025-11-27T10:00:00"
  }
}
```

**Errores:**
- `400 Bad Request`: Token de Firebase inválido
- `500 Internal Server Error`: Firebase no configurado

---

## 🔍 Verificación

### Verificar que Firebase está inicializado:

```bash
# Ver logs del backend
tail -f ~/app.log | grep -i firebase
```

**Log exitoso:**
```
✅ Firebase Admin SDK inicializado correctamente
```

**Log de error:**
```
❌ Error al inicializar Firebase Admin SDK: No se encontraron credenciales
```

### Probar el endpoint:

```bash
# Obtener un token de Firebase desde la app móvil y probar:
curl -X POST http://52.2.172.54:8080/api/v1/auth/firebase-sync \
  -H "Content-Type: application/json" \
  -d '{
    "firebaseIdToken": "TU_TOKEN_DE_FIREBASE_AQUI",
    "nombre": "Test",
    "apellidos": "Usuario"
  }'
```

---

## 📱 Flujo Completo

```
┌──────────────────────────────────────────────────────────────────┐
│                        FLUJO DE AUTENTICACIÓN                     │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. App Kotlin                                                    │
│     └── FirebaseAuth.signInWithEmailAndPassword(email, pass)      │
│         └── Firebase devuelve: FirebaseUser + ID Token            │
│                                                                   │
│  2. App Kotlin                                                    │
│     └── user.getIdToken(false)                                    │
│         └── Obtiene: "eyJhbGciOiJSUzI1NiIs..." (Firebase Token)   │
│                                                                   │
│  3. App Kotlin                                                    │
│     └── POST /api/v1/auth/firebase-sync                           │
│         └── Body: { firebaseIdToken: "...", nombre: "...", ... }  │
│                                                                   │
│  4. Backend Spring Boot                                           │
│     └── FirebaseAuth.verifyIdToken(firebaseIdToken)               │
│         └── Firebase valida el token                              │
│         └── Retorna: email, uid, displayName                      │
│                                                                   │
│  5. Backend Spring Boot                                           │
│     └── Buscar/Crear usuario en MySQL                             │
│     └── Generar JWT del backend                                   │
│     └── Retornar: { token: "JWT_BACKEND", user: {...} }           │
│                                                                   │
│  6. App Kotlin                                                    │
│     └── Guardar JWT_BACKEND en SharedPreferences                  │
│     └── Usar JWT_BACKEND para TODAS las requests al backend       │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Notas Importantes

1. **El token de Firebase solo se usa para `/auth/firebase-sync`**
2. **Para todos los demás endpoints, usar el JWT del backend**
3. **Los endpoints existentes (`/auth/login`, `/auth/register`) siguen funcionando**
4. **El campo `firebaseUid` se guarda para vincular usuarios de Firebase con la BD**

---

## 🔄 Despliegue en EC2

### Construir nuevo JAR:

```bash
# En tu máquina local
cd /Users/agustingarridosnoopy/Downloads/backend-spring-boot-latest
export JAVA_HOME=/Users/agustingarridosnoopy/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home
./mvnw clean package -DskipTests

# Subir a EC2
scp -i tu-key.pem target/huertohogar-api-0.0.1-SNAPSHOT.jar ec2-user@52.2.172.54:~/
```

### Desplegar:

```bash
ssh -i tu-key.pem ec2-user@52.2.172.54

# Detener proceso anterior
pkill -f huertohogar-api

# Iniciar con Firebase
export FIREBASE_CREDENTIALS_JSON='...'  # O usar archivo
nohup java -jar huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --firebase.enabled=true \
  > app.log 2>&1 &

# Verificar
tail -f app.log
```

---

*Documento generado el 27 de noviembre de 2025*
