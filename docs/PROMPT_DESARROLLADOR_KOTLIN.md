# Prompt para Desarrollador Frontend Kotlin (Android)
## Proyecto: Huerto Hogar - App Móvil

---

## 📋 CONTEXTO DEL PROYECTO

Necesitas desarrollar una aplicación Android en Kotlin que se integre con el backend de **Huerto Hogar**, una plataforma de venta de productos orgánicos (verduras y hortalizas). El backend ya está desplegado y funcionando.

### URLs del Sistema
- **API Base URL:** `http://52.2.172.54:8080/api/v1`
- **Swagger (Documentación interactiva):** `http://52.2.172.54:8080/swagger-ui/index.html`
- **OpenAPI Spec:** `http://52.2.172.54:8080/v3/api-docs`

---

## 🔐 AUTENTICACIÓN HÍBRIDA (Firebase + Backend JWT)

El sistema usa **autenticación híbrida**:
1. El usuario se autentica con **Firebase Authentication** en el móvil
2. Firebase devuelve un **ID Token**
3. El móvil envía ese token al backend via `/api/v1/auth/firebase-sync`
4. El backend valida el token con Firebase Admin SDK y genera un **JWT propio**
5. Usa ese JWT del backend para TODAS las demás requests

### Flujo de Autenticación

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   App Kotlin    │     │    Firebase     │     │  Backend Spring │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │ 1. signInWithEmail()  │                       │
         │──────────────────────>│                       │
         │                       │                       │
         │ 2. Firebase ID Token  │                       │
         │<──────────────────────│                       │
         │                       │                       │
         │ 3. POST /auth/firebase-sync                   │
         │   { firebaseIdToken: "...", datos opcionales }│
         │──────────────────────────────────────────────>│
         │                       │                       │
         │                       │ 4. Verificar token    │
         │                       │<──────────────────────│
         │                       │                       │
         │                       │ 5. Token válido ✓     │
         │                       │──────────────────────>│
         │                       │                       │
         │ 6. { token: "JWT_BACKEND", user: {...} }      │
         │<──────────────────────────────────────────────│
         │                       │                       │
         │ 7. Guardar JWT en SharedPreferences           │
         │                       │                       │
         │ 8. Usar JWT para todas las requests           │
         │   Authorization: Bearer {JWT_BACKEND}         │
         │──────────────────────────────────────────────>│
```

### Headers para Requests Protegidas
```
Authorization: Bearer {token_del_backend}
Content-Type: application/json
```

**IMPORTANTE:** El token que usas para las requests es el JWT del **backend**, NO el de Firebase.

---

## 📡 ENDPOINTS A IMPLEMENTAR

### 1. AUTENTICACIÓN (`/api/v1/auth`)

#### 1.1 🔥 Sincronización Firebase (NUEVO - USAR ESTE)
```
POST /api/v1/auth/firebase-sync
Content-Type: application/json

Request Body:
{
  "firebaseIdToken": "eyJhbGciOiJSUzI1NiIs...",  // REQUERIDO - Token de Firebase
  "run": "19.011.022-K",                          // Opcional (para registro inicial)
  "nombre": "Juan",                                // Opcional
  "apellidos": "Pérez González",                   // Opcional (¡PLURAL!)
  "direccion": "Av. Principal 123, Santiago",     // Opcional
  "telefono": "+56912345678"                       // Opcional
}

Response 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",   // ⬅️ JWT del BACKEND (usar este!)
  "user": {
    "email": "usuario@email.com",
    "nombre": "Juan",
    "apellido": "Pérez González",
    "run": "19.011.022-K",
    "direccion": "Av. Principal 123",
    "telefono": "+56912345678",
    "rol": "USER",
    "createdAt": "2025-11-27T10:00:00"
  }
}

Errores:
- 400 Bad Request: Token de Firebase inválido o expirado
- 500 Internal Server Error: Firebase no configurado en servidor
```

#### 1.2 Registro Tradicional (alternativo, sin Firebase)
```
POST /api/v1/auth/register
Content-Type: application/json

Request Body:
{
  "run": "19.011.022-K",           // String, obligatorio (RUT chileno)
  "nombre": "Juan",                 // String, obligatorio, 2-50 chars
  "apellidos": "Pérez González",    // String, obligatorio, 2-50 chars (¡OJO: PLURAL!)
  "email": "juan@ejemplo.com",      // String, obligatorio, formato email válido
  "password": "MiClave123!",        // String, obligatorio, min 8 chars + 1 especial
  "direccion": "Av. Principal 123", // String, opcional
  "telefono": "+56912345678"        // String, opcional
}

Response 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { ... }
}
```

#### 1.3 Login Tradicional (alternativo, sin Firebase)
```
POST /api/v1/auth/login
Content-Type: application/json

Request Body:
{
  "email": "juan@ejemplo.com",
  "password": "MiClave123!"
}

Response 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { ... }
}
```

---

### 2. PRODUCTOS (`/api/v1/products`)

#### 2.1 Listar Todos los Productos
```
GET /api/v1/products
Authorization: Bearer {token}

Query Params opcionales:
- q: String (búsqueda por nombre/descripción)

Response 200 OK:
[
  {
    "id": "1",
    "codigo": "VH-001",
    "nombre": "Lechuga Hidropónica",
    "descripcion": "Lechuga fresca cultivada en sistema hidropónico",
    "precio": 1500.0,
    "stock": 50,
    "imagen": "https://ejemplo.com/lechuga.jpg",
    "categoria": "Verduras",
    "createdAt": null
  },
  // ... más productos
]
```

#### 2.2 Obtener Producto por ID
```
GET /api/v1/products/{id}
Authorization: Bearer {token}

Response 200 OK:
{
  "id": "1",
  "codigo": "VH-001",
  "nombre": "Lechuga Hidropónica",
  "descripcion": "Lechuga fresca cultivada en sistema hidropónico",
  "precio": 1500.0,
  "stock": 50,
  "imagen": "https://ejemplo.com/lechuga.jpg",
  "categoria": "Verduras",
  "createdAt": null
}

Errores:
- 404 Not Found: Producto no existe
```

#### 2.3 Obtener Producto por Código
```
GET /api/v1/products/codigo/{codigo}
Authorization: Bearer {token}

Ejemplo: GET /api/v1/products/codigo/VH-001

Response 200 OK:
{
  "id": "1",
  "codigo": "VH-001",
  "nombre": "Lechuga Hidropónica",
  ...
}
```

#### 2.4 Buscar Productos
```
GET /api/v1/products?q=lechuga
Authorization: Bearer {token}

Response 200 OK:
[
  // Array de productos que coinciden con la búsqueda
]
```

---

### 3. PEDIDOS (`/api/v1/orders`)

#### 3.1 Crear Pedido
```
POST /api/v1/orders
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "direccionEntrega": "Lamarck 36, Valparaíso",  // String, obligatorio
  "fechaEntrega": "2025-12-01",                   // String ISO date, opcional (debe ser futuro)
  "region": "Región de Valparaíso",               // String, opcional
  "comuna": "Valparaíso",                         // String, opcional
  "comentarios": "Dejar en conserjería",          // String, opcional
  "items": [                                       // Array, obligatorio (min 1 item)
    {
      "productoId": "VH-001",                     // String (código del producto, NO el id numérico)
      "cantidad": 2                                // Integer, obligatorio, > 0
    },
    {
      "productoId": "VH-002",
      "cantidad": 1
    }
  ]
}

Response 200 OK:
{
  "id": 17,
  "user": {
    "email": "juan@ejemplo.com",
    "nombre": "Juan",
    ...
  },
  "items": [
    {
      "id": 45,
      "productoId": "VH-001",
      "cantidad": 2,
      "precioUnitario": 1500.0
    }
  ],
  "total": 4800.0,
  "estado": "PENDIENTE",
  "direccionEntrega": "Lamarck 36, Valparaíso",
  "region": "Región de Valparaíso",
  "comuna": "Valparaíso",
  "comentarios": "Dejar en conserjería",
  "fechaEntrega": "2025-12-01",
  "createdAt": "2025-11-27T12:00:00"
}

Errores:
- 400 Bad Request: Datos inválidos o producto no encontrado
- 401 Unauthorized: Token inválido
```

#### 3.2 Listar Mis Pedidos
```
GET /api/v1/orders
Authorization: Bearer {token}

Response 200 OK:
[
  {
    "id": 17,
    "user": {...},
    "items": [...],
    "total": 4800.0,
    "estado": "PENDIENTE",
    "direccionEntrega": "...",
    "fechaEntrega": "2025-12-01",
    "createdAt": "2025-11-27T12:00:00"
  },
  // ... más pedidos
]
```

#### 3.3 Obtener Pedido por ID
```
GET /api/v1/orders/{id}
Authorization: Bearer {token}

Response 200 OK:
{
  "id": 17,
  "user": {...},
  "items": [...],
  "total": 4800.0,
  "estado": "CONFIRMADO",
  ...
}

Errores:
- 403 Forbidden: No es tu pedido
- 404 Not Found: Pedido no existe
```

---

### 4. PERFIL DE USUARIO (`/api/v1/users`)

#### 4.1 Obtener Mi Perfil
```
GET /api/v1/users/me
Authorization: Bearer {token}

Response 200 OK:
{
  "email": "juan@ejemplo.com",
  "nombre": "Juan",
  "apellido": "Pérez González",
  "run": "19.011.022-K",
  "direccion": "Av. Principal 123",
  "telefono": "+56912345678",
  "rol": "USER",
  "createdAt": "2025-11-27T10:00:00"
}
```

---

## 📊 DATA CLASSES KOTLIN SUGERIDAS

```kotlin
// === AUTH ===

// 🔥 Para autenticación híbrida Firebase (RECOMENDADO)
data class FirebaseSyncRequest(
    val firebaseIdToken: String,    // Token de Firebase (obligatorio)
    val run: String? = null,        // Opcional
    val nombre: String? = null,     // Opcional
    val apellidos: String? = null,  // Opcional (¡PLURAL!)
    val direccion: String? = null,  // Opcional
    val telefono: String? = null    // Opcional
)

// Para registro tradicional (sin Firebase)
data class RegisterRequest(
    val run: String,
    val nombre: String,
    val apellidos: String,  // ¡IMPORTANTE: plural!
    val email: String,
    val password: String,
    val direccion: String? = null,
    val telefono: String? = null
)

// Para login tradicional (sin Firebase)
data class LoginRequest(
    val email: String,
    val password: String
)

// Response común para auth
data class AuthResponse(
    val token: String,      // JWT del BACKEND (usar este para todo!)
    val user: UserResponse
)

data class UserResponse(
    val email: String,
    val nombre: String,
    val apellido: String,
    val run: String?,
    val direccion: String?,
    val telefono: String?,
    val rol: String,
    val createdAt: String?
)

// === PRODUCTOS ===
data class ProductResponse(
    val id: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagen: String?,
    val categoria: String?,
    val createdAt: String?
)

// === PEDIDOS ===
data class OrderItemRequest(
    val productoId: String,  // Código del producto (ej: "VH-001")
    val cantidad: Int
)

data class OrderRequest(
    val direccionEntrega: String,
    val fechaEntrega: String?,  // Formato: "2025-12-01"
    val region: String? = null,
    val comuna: String? = null,
    val comentarios: String? = null,
    val items: List<OrderItemRequest>
)

data class OrderItemResponse(
    val id: Long,
    val productoId: String,
    val cantidad: Int,
    val precioUnitario: Double
)

data class OrderResponse(
    val id: Long,
    val user: UserResponse,
    val items: List<OrderItemResponse>,
    val total: Double,
    val estado: String,  // PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
    val direccionEntrega: String,
    val region: String?,
    val comuna: String?,
    val comentarios: String?,
    val fechaEntrega: String?,
    val createdAt: String?
)
```

---

## 🔄 ESTADOS DE PEDIDO (Enum)

```kotlin
enum class OrderStatus(val descripcion: String) {
    PENDIENTE("Pendiente de confirmación"),
    CONFIRMADO("Confirmado por administrador"),
    ENVIADO("En camino al cliente"),
    ENTREGADO("Entregado al cliente"),
    CANCELADO("Pedido cancelado")
}
```

---

## 🛠️ CONFIGURACIÓN RETROFIT SUGERIDA

```kotlin
// ApiService.kt
interface HuertoHogarApi {
    
    // Auth - Firebase (RECOMENDADO)
    @POST("auth/firebase-sync")
    suspend fun firebaseSync(@Body request: FirebaseSyncRequest): Response<AuthResponse>
    
    // Auth - Tradicional (alternativo)
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    // Products
    @GET("products")
    suspend fun getProducts(@Query("q") search: String? = null): Response<List<ProductResponse>>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductResponse>
    
    @GET("products/codigo/{codigo}")
    suspend fun getProductByCodigo(@Path("codigo") codigo: String): Response<ProductResponse>
    
    // Orders
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>
    
    @GET("orders")
    suspend fun getMyOrders(): Response<List<OrderResponse>>
    
    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<OrderResponse>
    
    // User
    @GET("users/me")
    suspend fun getMyProfile(): Response<UserResponse>
}

// RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "http://52.2.172.54:8080/api/v1/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Obtener el JWT del BACKEND (no el de Firebase!)
            val token = TokenManager.getBackendToken()
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        .build()
    
    val api: HuertoHogarApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuertoHogarApi::class.java)
    }
}

// TokenManager.kt - Ejemplo de gestión de tokens
object TokenManager {
    private var backendToken: String? = null
    
    fun saveBackendToken(token: String) {
        backendToken = token
        // También guardar en SharedPreferences/DataStore para persistencia
    }
    
    fun getBackendToken(): String? = backendToken
    
    fun clearToken() {
        backendToken = null
    }
}
```

## 🔥 INTEGRACIÓN CON FIREBASE AUTH

```kotlin
// AuthRepository.kt
class AuthRepository(
    private val api: HuertoHogarApi,
    private val firebaseAuth: FirebaseAuth
) {
    
    /**
     * Login con Firebase + sincronización con backend
     */
    suspend fun loginWithFirebase(email: String, password: String): Result<AuthResponse> {
        return try {
            // 1. Autenticar con Firebase
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            
            // 2. Obtener el ID Token de Firebase
            val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("No se pudo obtener token de Firebase"))
            
            // 3. Sincronizar con el backend
            val response = api.firebaseSync(FirebaseSyncRequest(
                firebaseIdToken = firebaseToken
            ))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // 4. Guardar el JWT del BACKEND (no el de Firebase!)
                TokenManager.saveBackendToken(authResponse.token)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en sincronización: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registro con Firebase + sincronización con backend
     */
    suspend fun registerWithFirebase(
        email: String, 
        password: String,
        nombre: String,
        apellidos: String,
        run: String? = null,
        direccion: String? = null,
        telefono: String? = null
    ): Result<AuthResponse> {
        return try {
            // 1. Crear usuario en Firebase
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            // 2. Obtener el ID Token de Firebase
            val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("No se pudo obtener token de Firebase"))
            
            // 3. Sincronizar con backend (enviando datos adicionales)
            val response = api.firebaseSync(FirebaseSyncRequest(
                firebaseIdToken = firebaseToken,
                nombre = nombre,
                apellidos = apellidos,  // ¡PLURAL!
                run = run,
                direccion = direccion,
                telefono = telefono
            ))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                TokenManager.saveBackendToken(authResponse.token)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en registro: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si falla, intentar eliminar usuario de Firebase
            firebaseAuth.currentUser?.delete()
            Result.failure(e)
        }
    }
    
    /**
     * Login con Google + sincronización con backend
     */
    suspend fun loginWithGoogle(googleIdToken: String): Result<AuthResponse> {
        return try {
            // 1. Autenticar con Firebase usando credenciales de Google
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            
            // 2. Obtener el ID Token de Firebase
            val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("No se pudo obtener token de Firebase"))
            
            // 3. Sincronizar con backend
            val response = api.firebaseSync(FirebaseSyncRequest(
                firebaseIdToken = firebaseToken
            ))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                TokenManager.saveBackendToken(authResponse.token)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en login con Google: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        firebaseAuth.signOut()
        TokenManager.clearToken()
    }
}
```
```

---

## 📱 PANTALLAS SUGERIDAS

1. **Splash Screen** → Verificar si hay token guardado
2. **Login Screen** → Formulario email + password
3. **Register Screen** → Formulario completo de registro
4. **Home/Products Screen** → Lista de productos con búsqueda
5. **Product Detail Screen** → Detalle de producto + agregar al carrito
6. **Cart Screen** → Carrito de compras local
7. **Checkout Screen** → Formulario de dirección de entrega
8. **Orders Screen** → Lista de mis pedidos
9. **Order Detail Screen** → Detalle de pedido con estado
10. **Profile Screen** → Ver/editar perfil

---

## ⚠️ PUNTOS CRÍTICOS A TENER EN CUENTA

### 1. Campo `apellidos` (PLURAL)
```kotlin
// ✅ CORRECTO
data class RegisterRequest(
    val apellidos: String  // CON 'S' AL FINAL
)

// ❌ INCORRECTO
data class RegisterRequest(
    val apellido: String   // SIN 'S' - NO FUNCIONARÁ
)
```

### 2. Campo `productoId` usa CÓDIGO, no ID numérico
```kotlin
// ✅ CORRECTO
OrderItemRequest(
    productoId = "VH-001",  // Código del producto
    cantidad = 2
)

// ❌ INCORRECTO  
OrderItemRequest(
    productoId = "1",       // ID numérico - NO FUNCIONARÁ
    cantidad = 2
)
```

### 3. Fecha de entrega debe ser futura
```kotlin
// ✅ CORRECTO
fechaEntrega = "2025-12-01"  // Fecha futura

// ❌ INCORRECTO
fechaEntrega = "2024-11-30"  // Fecha pasada - ERROR 400
```

### 4. Password requiere carácter especial
```kotlin
// ✅ CORRECTO
password = "MiClave123!"  // Tiene ! como especial

// ❌ INCORRECTO
password = "MiClave123"   // Sin especial - ERROR 400
```

---

## 🧪 CREDENCIALES DE PRUEBA

```
Usuario de prueba:
- Email: nuevo@test.com
- Password: Test1234!

Admin (solo para referencia):
- Email: superadmin@huertohogar.cl
- Password: Admin123!
```

---

## 📦 CÓDIGOS DE PRODUCTOS DISPONIBLES

| Código | Nombre | Precio |
|--------|--------|--------|
| VH-001 | Lechuga Hidropónica | $1,500 |
| VH-002 | Tomate Cherry Orgánico | $2,500 |
| VH-003 | Espinaca Baby | $1,800 |
| VH-004 | Zanahoria Orgánica | $1,200 |
| VH-005 | Pepino Hidropónico | $1,600 |
| TM-001 | Tomate | $2,000 |
| TM-002 | Tomate Limachino | $2,200 |
| TM-003 | Tomate Cherry Mix | $2,800 |
| RT-001 | Rúcula | $1,700 |
| RT-002 | Rúcula Silvestre | $1,900 |
| RT-003 | Mix de Hojas | $2,100 |

---

## 🚀 FLUJO TÍPICO DE LA APP

```
1. Usuario abre app
   └── Verificar token en SharedPreferences
       ├── Si hay token válido → Home Screen
       └── Si no hay token → Login Screen

2. Login/Register
   └── POST /auth/login o /register
       └── Guardar token + user en SharedPreferences
           └── Navegar a Home Screen

3. Ver Productos
   └── GET /products
       └── Mostrar lista con RecyclerView/LazyColumn

4. Agregar al Carrito (local)
   └── Guardar en Room/SharedPreferences
       └── Actualizar badge del carrito

5. Checkout
   └── POST /orders con items del carrito
       └── Limpiar carrito local
           └── Navegar a Order Detail

6. Ver Mis Pedidos
   └── GET /orders
       └── Mostrar lista con estados
```

---

## 📞 SOPORTE

- **Swagger UI:** http://52.2.172.54:8080/swagger-ui/index.html
- **Backend Status:** http://52.2.172.54:8080/actuator/health

---

## 🧪 CÓDIGO DE VALIDACIÓN RÁPIDA (KOTLIN)

Usa este código para validar que la integración Firebase + Backend funciona correctamente:

### build.gradle.kts (app level)
```kotlin
dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

### ApiService.kt
```kotlin
interface HuertoHogarApi {
    
    @POST("auth/firebase-sync")
    suspend fun syncFirebaseUser(
        @Body request: FirebaseSyncRequest
    ): Response<AuthResponse>
    
    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String
    ): Response<List<Product>>
    
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body order: CreateOrderRequest
    ): Response<Order>
}
```

### DTOs.kt
```kotlin
// Request para sincronizar Firebase
data class FirebaseSyncRequest(
    val firebaseIdToken: String,
    val run: String? = null,
    val nombre: String? = null,
    val apellidos: String? = null,  // ⚠️ PLURAL!
    val direccion: String? = null,
    val telefono: String? = null
)

// Response de autenticación
data class AuthResponse(
    val token: String,    // ⬅️ Este es el JWT del backend
    val user: User
)

data class User(
    val email: String,
    val nombre: String,
    val apellido: String,
    val run: String?,
    val direccion: String?,
    val telefono: String?,
    val rol: String
)

// Producto
data class Product(
    val id: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagen: String?,
    val categoria: String?
)

// Crear pedido
data class CreateOrderRequest(
    val direccionEntrega: String,
    val items: List<OrderItemRequest>,
    val region: String? = null,
    val comuna: String? = null,
    val comentarios: String? = null
)

data class OrderItemRequest(
    val productoId: String,  // Usar CÓDIGO del producto (ej: "VH-001")
    val cantidad: Int
)
```

### AuthRepository.kt
```kotlin
class AuthRepository(
    private val api: HuertoHogarApi,
    private val prefs: SharedPreferences
) {
    
    /**
     * Sincroniza usuario Firebase con el backend.
     * Guarda el JWT del backend para requests futuras.
     */
    suspend fun syncWithBackend(
        firebaseUser: FirebaseUser,
        additionalData: Map<String, String>? = null
    ): Result<AuthResponse> {
        return try {
            // 1. Obtener token de Firebase
            val firebaseToken = firebaseUser.getIdToken(false).await().token
                ?: return Result.failure(Exception("No se pudo obtener token de Firebase"))
            
            // 2. Preparar request
            val request = FirebaseSyncRequest(
                firebaseIdToken = firebaseToken,
                run = additionalData?.get("run"),
                nombre = additionalData?.get("nombre") ?: firebaseUser.displayName?.split(" ")?.firstOrNull(),
                apellidos = additionalData?.get("apellidos") ?: firebaseUser.displayName?.split(" ")?.drop(1)?.joinToString(" "),
                direccion = additionalData?.get("direccion"),
                telefono = additionalData?.get("telefono") ?: firebaseUser.phoneNumber
            )
            
            // 3. Llamar al backend
            val response = api.syncFirebaseUser(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // 4. Guardar JWT del BACKEND (no el de Firebase)
                prefs.edit()
                    .putString("backend_jwt", authResponse.token)
                    .putString("user_email", authResponse.user.email)
                    .apply()
                
                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getBackendToken(): String? = prefs.getString("backend_jwt", null)
    
    fun getAuthHeader(): String = "Bearer ${getBackendToken()}"
}
```

### Uso en ViewModel/Activity
```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                // 1. Login con Firebase
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user ?: throw Exception("Usuario no encontrado")
                
                // 2. Sincronizar con backend
                val syncResult = authRepository.syncWithBackend(firebaseUser)
                
                syncResult.fold(
                    onSuccess = { authResponse ->
                        Log.d("Login", "✅ Usuario sincronizado: ${authResponse.user.email}")
                        Log.d("Login", "✅ JWT Backend: ${authResponse.token.take(50)}...")
                        // Navegar a home
                    },
                    onFailure = { error ->
                        Log.e("Login", "❌ Error sincronizando: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("Login", "❌ Error en Firebase login: ${e.message}")
            }
        }
    }
    
    fun registerWithEmail(
        email: String, 
        password: String,
        nombre: String,
        apellidos: String,
        run: String
    ) {
        viewModelScope.launch {
            try {
                // 1. Crear usuario en Firebase
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user ?: throw Exception("Error creando usuario")
                
                // 2. Sincronizar con backend (incluir datos adicionales)
                val additionalData = mapOf(
                    "nombre" to nombre,
                    "apellidos" to apellidos,  // ⚠️ PLURAL
                    "run" to run
                )
                
                val syncResult = authRepository.syncWithBackend(firebaseUser, additionalData)
                
                syncResult.fold(
                    onSuccess = { authResponse ->
                        Log.d("Register", "✅ Usuario registrado: ${authResponse.user.email}")
                    },
                    onFailure = { error ->
                        Log.e("Register", "❌ Error: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("Register", "❌ Error Firebase: ${e.message}")
            }
        }
    }
}
```

### Test rápido de conexión
```kotlin
// Ejecutar en onCreate o en un test
suspend fun testBackendConnection() {
    val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        })
        .build()
    
    val retrofit = Retrofit.Builder()
        .baseUrl("http://52.2.172.54:8080/api/v1/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api = retrofit.create(HuertoHogarApi::class.java)
    
    // Test 1: Verificar productos (endpoint público)
    try {
        val products = api.getProducts("") // Sin token para ver si responde
        Log.d("Test", "Productos: ${products.body()?.size ?: "error"}")
    } catch (e: Exception) {
        Log.e("Test", "Error productos: ${e.message}")
    }
}
```

---

## ⚠️ ERRORES COMUNES Y SOLUCIONES

| Error | Causa | Solución |
|-------|-------|----------|
| `400 - Token de Firebase inválido` | Token expirado o mal formado | Obtener nuevo token con `getIdToken(true)` |
| `401 - Unauthorized` | JWT del backend expirado | Volver a sincronizar con Firebase |
| `403 - Forbidden` | Usuario sin permisos | Verificar rol del usuario |
| `apellidos` vs `apellido` | Confusión en nombre del campo | Request usa `apellidos` (plural), response usa `apellido` (singular) |
| `productoId` incorrecto | Usar ID numérico en vez de código | Usar el CÓDIGO del producto (ej: "VH-001", no "1") |

---

## 🔒 CONFIGURACIÓN FIREBASE (google-services.json)

El proyecto Firebase es: **huerto-hogar-cbe8d**

1. Ve a [Firebase Console](https://console.firebase.google.com/project/huerto-hogar-cbe8d)
2. Configuración del proyecto → Tus apps
3. Si no existe, registra la app Android con tu package name
4. Descarga `google-services.json`
5. Colócalo en `app/google-services.json`

### Habilitar Authentication
1. Firebase Console → Authentication → Sign-in method
2. Habilita **Email/Password**
3. (Opcional) Habilita **Google Sign-In**

---

*Documento actualizado el 27 de noviembre de 2025*
*Backend Version: 1.0.0*
*Firebase Project: huerto-hogar-cbe8d*
