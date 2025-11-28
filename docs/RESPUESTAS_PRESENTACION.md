# 📋 Respuestas para la Presentación - Huerto Hogar

## 1. 📱 APK FIRMADO

### ¿Dónde muestro el archivo APK firmado?
El APK firmado se encuentra en:
```
app/build/outputs/apk/release/app-release.apk
```
O si usas Bundle (recomendado para Play Store):
```
app/build/outputs/bundle/release/app-release.aab
```

### ¿Cómo se genera el APK firmado paso a paso?

**Opción 1: Desde Android Studio (Recomendado)**
1. `Build` → `Generate Signed Bundle / APK`
2. Seleccionar `APK` o `Android App Bundle`
3. **Crear keystore** (primera vez):
   - Click en `Create new...`
   - Elegir ubicación: `~/keystore/huerto-hogar.jks`
   - Contraseña del keystore: (guardar segura)
   - Alias: `huerto-hogar-key`
   - Contraseña del key: (guardar segura)
   - Llenar datos del certificado (nombre, organización, país)
4. Seleccionar `release` como Build Variant
5. Click en `Finish`

**Opción 2: Desde Terminal**
```bash
# 1. Generar keystore (solo primera vez)
keytool -genkey -v -keystore huerto-hogar.jks -keyalg RSA -keysize 2048 -validity 10000 -alias huerto-hogar-key

# 2. Configurar en build.gradle.kts (app)
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/huerto-hogar.jks")
            storePassword = "tu_password"
            keyAlias = "huerto-hogar-key"
            keyPassword = "tu_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(...)
        }
    }
}

# 3. Generar APK
./gradlew assembleRelease
```

### Importancia para distribución en dispositivos reales

| Aspecto | Sin Firmar (Debug) | Firmado (Release) |
|---------|-------------------|-------------------|
| **Instalación** | Solo con USB/ADB | Cualquier dispositivo |
| **Play Store** | ❌ No permitido | ✅ Requerido |
| **Seguridad** | Certificado debug genérico | Certificado único del desarrollador |
| **Actualizaciones** | No garantizadas | Solo con misma firma |
| **Integridad** | No verificable | Google verifica autenticidad |
| **APIs sensibles** | Limitadas | Acceso completo (Maps, Firebase, etc.) |

**Puntos clave:**
- El APK firmado **garantiza que la app no fue modificada** por terceros
- Permite **actualizaciones automáticas** (mismo certificado)
- **Requerido por Google Play** y otras tiendas
- Habilita **APIs de producción** (Firebase, Google Maps, etc.)

---

## 2. 🔄 API PÚBLICA vs MICROSERVICIOS PROPIOS

### Diferencia clave

| Aspecto | API Pública (ej: OpenWeather) | Microservicios Propios (nuestro backend) |
|---------|------------------------------|------------------------------------------|
| **Control** | Ninguno - dependemos del proveedor | Total - nosotros definimos todo |
| **Datos** | Solo lectura generalmente | CRUD completo sobre nuestros datos |
| **Personalización** | Limitada a lo que ofrecen | 100% adaptada a nuestras necesidades |
| **Autenticación** | API Key del proveedor | JWT propio + Firebase Auth |
| **Costo** | Planes de pago por uso | Costo de infraestructura (EC2) |
| **Disponibilidad** | Dependemos de ellos | Controlamos uptime |
| **Ejemplo en app** | - | `/api/v1/products`, `/api/v1/orders` |

### En Huerto Hogar usamos:
- **Microservicios propios**: Backend Spring Boot en EC2 para productos, pedidos, usuarios
- **Servicios externos**: Firebase Auth (autenticación), Firebase Firestore (tiempo real)

---

## 3. 🛠️ JUSTIFICACIÓN DE ENDPOINTS

### Endpoints implementados y su justificación:

#### **Productos** (`/api/v1/products`)
| Método | Endpoint | Justificación | Uso en App |
|--------|----------|---------------|------------|
| `GET` | `/products` | Listar catálogo para usuarios | Pantalla principal, búsqueda |
| `GET` | `/products/{id}` | Ver detalle de producto | Pantalla detalle producto |
| `POST` | `/products` | Admin crea productos | Panel administrador |
| `PUT` | `/products/{id}` | Admin actualiza stock/precio | Gestión de inventario |
| `DELETE` | `/products/{id}` | Admin elimina productos | Limpieza de catálogo |

#### **Pedidos** (`/api/v1/orders`)
| Método | Endpoint | Justificación | Uso en App |
|--------|----------|---------------|------------|
| `GET` | `/orders` | Usuario ve sus pedidos | "Mis Pedidos" |
| `GET` | `/orders/{id}` | Detalle de un pedido | Seguimiento |
| `POST` | `/orders` | Crear nuevo pedido | Checkout del carrito |
| `PUT` | `/orders/{id}/status` | Admin cambia estado | Gestión de pedidos |
| `PUT` | `/orders/{id}/cancel` | Usuario cancela pedido | Cancelar si está pendiente |

#### **Usuarios** (`/api/v1/users`)
| Método | Endpoint | Justificación | Uso en App |
|--------|----------|---------------|------------|
| `GET` | `/users/me` | Obtener perfil actual | Pantalla perfil |
| `PUT` | `/users/me` | Actualizar perfil | Editar datos personales |
| `GET` | `/users` | Admin lista usuarios | Panel admin |
| `DELETE` | `/users/{email}` | Admin elimina usuario | Gestión usuarios |

#### **Autenticación** (`/api/v1/auth`)
| Método | Endpoint | Justificación | Uso en App |
|--------|----------|---------------|------------|
| `POST` | `/auth/register` | Registro tradicional | Crear cuenta |
| `POST` | `/auth/login` | Login tradicional | Iniciar sesión |
| `POST` | `/auth/firebase-sync` | Sincronizar Firebase con backend | Login con Google/Firebase |

### Integración desde la App Móvil (Kotlin)

```kotlin
// Ejemplo de integración en la app
class ProductRepository(private val api: HuertoHogarApi) {
    
    // GET - Listar productos
    suspend fun getProducts(): List<Product> {
        return api.getProducts()  // Retrofit hace GET /api/v1/products
    }
    
    // POST - Crear pedido
    suspend fun createOrder(order: OrderRequest): Order {
        return api.createOrder(order)  // POST /api/v1/orders
    }
}

// Retrofit Interface
interface HuertoHogarApi {
    @GET("products")
    suspend fun getProducts(): List<Product>
    
    @POST("orders")
    suspend fun createOrder(@Body order: OrderRequest): Order
    
    @PUT("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") orderId: Long): Order
}
```

---

## 4. 🎨 INTEGRACIÓN API AL FLUJO VISUAL

### Flujo de la App:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   SplashScreen  │────▶│  LoginScreen    │────▶│   HomeScreen    │
│                 │     │  (Firebase Auth)│     │ GET /products   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                        │
                        ┌───────────────────────────────┼───────────────────┐
                        ▼                               ▼                   ▼
                ┌───────────────┐              ┌───────────────┐    ┌───────────────┐
                │ ProductDetail │              │    Cart       │    │   Profile     │
                │GET /products/1│              │  (Local)      │    │ GET /users/me │
                └───────────────┘              └───────────────┘    └───────────────┘
                                                        │
                                                        ▼
                                               ┌───────────────┐
                                               │   Checkout    │
                                               │ POST /orders  │
                                               └───────────────┘
                                                        │
                                                        ▼
                                               ┌───────────────┐
                                               │  OrderHistory │
                                               │  GET /orders  │
                                               └───────────────┘
```

### Justificación de la integración:

1. **LoginScreen** → `POST /auth/firebase-sync`
   - Usuario inicia sesión con Firebase
   - App envía token a backend para obtener JWT propio
   - JWT se guarda localmente para futuras peticiones

2. **HomeScreen** → `GET /products`
   - Al cargar, muestra lista de productos del backend
   - Permite búsqueda y filtrado local
   - Pull-to-refresh actualiza desde servidor

3. **ProductDetailScreen** → `GET /products/{id}`
   - Muestra información completa del producto
   - Botón "Agregar al carrito" guarda en Room (local)

4. **CartScreen** → Sin API (local)
   - Carrito guardado en Room Database
   - Permite modificar cantidades offline

5. **CheckoutScreen** → `POST /orders`
   - Envía pedido al backend
   - Backend valida stock y crea pedido
   - Respuesta incluye número de pedido

6. **OrderHistoryScreen** → `GET /orders`
   - Lista pedidos del usuario autenticado
   - Muestra estado actualizado desde servidor

---

## 5. 💾 PERSISTENCIA DE DATOS

### Almacenamiento Local vs Externo

```
┌─────────────────────────────────────────────────────────────────┐
│                        APP MÓVIL (Kotlin)                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐    ┌─────────────────────────────────┐ │
│  │  ALMACENAMIENTO     │    │     ALMACENAMIENTO EXTERNO      │ │
│  │      LOCAL          │    │     (Backend + Firebase)        │ │
│  ├─────────────────────┤    ├─────────────────────────────────┤ │
│  │                     │    │                                 │ │
│  │  📦 Room Database   │    │  🌐 Backend Spring Boot (EC2)   │ │
│  │  - Carrito          │    │  - Productos (MySQL)            │ │
│  │  - Productos cache  │    │  - Pedidos (MySQL)              │ │
│  │  - Favoritos        │    │  - Usuarios (MySQL)             │ │
│  │                     │    │                                 │ │
│  │  🔐 DataStore       │    │  🔥 Firebase                    │ │
│  │  - JWT Token        │    │  - Autenticación                │ │
│  │  - Preferencias     │    │  - Firestore (tiempo real)      │ │
│  │  - Tema oscuro      │    │                                 │ │
│  │                     │    │                                 │ │
│  └─────────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Detalle de cada tipo:

#### **Almacenamiento LOCAL (en el dispositivo)**

| Tecnología | Datos | Justificación |
|------------|-------|---------------|
| **Room Database** | Carrito de compras | Permite agregar productos offline, sincroniza al hacer checkout |
| **Room Database** | Cache de productos | Muestra productos sin internet, actualiza cuando hay conexión |
| **DataStore** | JWT Token | Acceso rápido para autenticar peticiones |
| **DataStore** | Preferencias usuario | Tema, idioma, notificaciones |

```kotlin
// Ejemplo Room - Entidad Carrito
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    val nombre: String,
    val precio: Double,
    val cantidad: Int,
    val imagen: String
)

// Ejemplo DataStore - Guardar token
suspend fun saveToken(token: String) {
    dataStore.edit { preferences ->
        preferences[TOKEN_KEY] = token
    }
}
```

#### **Almacenamiento EXTERNO (servidor)**

| Tecnología | Datos | Justificación |
|------------|-------|---------------|
| **MySQL (EC2)** | Productos | Fuente de verdad, sincronizado entre todos los usuarios |
| **MySQL (EC2)** | Pedidos | Historial persistente, accesible desde cualquier dispositivo |
| **MySQL (EC2)** | Usuarios | Perfiles, roles, autenticación |
| **Firebase Auth** | Credenciales | Autenticación segura gestionada por Google |
| **Firebase Firestore** | Pedidos (sync) | Actualización en tiempo real para app admin |

```sql
-- Ejemplo MySQL - Tabla de Pedidos
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_email VARCHAR(255) REFERENCES users(email),
    total DECIMAL(10,2),
    estado ENUM('PENDIENTE','CONFIRMADO','EN_CAMINO','ENTREGADO','CANCELADO'),
    direccion_entrega TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### ¿Por qué esta separación?

| Local | Externo |
|-------|---------|
| ✅ Funciona offline | ✅ Datos sincronizados entre dispositivos |
| ✅ Respuesta instantánea | ✅ Fuente de verdad única |
| ✅ Reduce consumo de datos | ✅ Backup automático |
| ✅ Mejor UX (carrito persistente) | ✅ Accesible desde web/admin |

---

## 6. 📁 ESTRUCTURA DE CARPETAS DEL BACKEND

```
backend-spring-boot-latest/
│
├── 📄 pom.xml                 # Dependencias Maven (Spring Boot, Firebase, JWT, etc.)
├── 📄 Dockerfile              # Para desplegar en contenedor
├── 📄 mvnw                    # Maven wrapper (ejecutar sin instalar Maven)
│
├── 📁 src/main/java/cl/huertohogar/huertohogar_api/
│   │
│   ├── 📄 HuertohogarApiApplication.java  # 🚀 PUNTO DE ENTRADA
│   │                                       # Inicia Spring Boot, habilita @EnableAsync
│   │
│   ├── 📁 config/                          # ⚙️ CONFIGURACIONES
│   │   ├── SecurityConfig.java            # Seguridad: rutas públicas/privadas, JWT filter
│   │   ├── FirebaseConfig.java            # Inicializa Firebase Admin SDK
│   │   ├── CorsConfig.java                # Permite peticiones desde app móvil
│   │   └── OpenApiConfig.java             # Swagger/OpenAPI documentación
│   │
│   ├── 📁 controller/                      # 🎯 ENDPOINTS (reciben peticiones HTTP)
│   │   ├── AuthController.java            # /auth/login, /auth/register, /auth/firebase-sync
│   │   ├── ProductController.java         # /products CRUD
│   │   ├── OrderController.java           # /orders CRUD
│   │   ├── UserController.java            # /users CRUD
│   │   └── DocumentoController.java       # /documentos (subida S3)
│   │
│   ├── 📁 service/                         # 🧠 LÓGICA DE NEGOCIO
│   │   ├── AuthService.java               # Login, registro, generar JWT
│   │   ├── FirebaseAuthService.java       # Validar tokens Firebase, sincronizar usuarios
│   │   ├── ProductService.java            # CRUD productos
│   │   ├── OrderService.java              # CRUD pedidos, validar stock
│   │   ├── UserService.java               # CRUD usuarios
│   │   └── FirestoreSyncService.java      # Sincronizar con Firestore (tiempo real)
│   │
│   ├── 📁 repository/                      # 🗄️ ACCESO A BASE DE DATOS
│   │   ├── UserRepository.java            # JPA queries para usuarios
│   │   ├── ProductRepository.java         # JPA queries para productos
│   │   └── OrderRepository.java           # JPA queries para pedidos
│   │
│   ├── 📁 model/                           # 📊 ENTIDADES (tablas de BD)
│   │   ├── User.java                      # @Entity - tabla users
│   │   ├── Product.java                   # @Entity - tabla products
│   │   ├── Order.java                     # @Entity - tabla orders
│   │   ├── OrderItem.java                 # @Entity - items de cada pedido
│   │   ├── Role.java                      # Enum: USER, ADMIN
│   │   └── OrderStatus.java               # Enum: PENDIENTE, CONFIRMADO, etc.
│   │
│   ├── 📁 dto/                             # 📨 OBJETOS DE TRANSFERENCIA
│   │   ├── LoginRequest.java              # { email, password }
│   │   ├── RegisterRequest.java           # { email, password, nombre, ... }
│   │   ├── AuthResponse.java              # { token, user }
│   │   ├── OrderRequest.java              # { items, direccion, ... }
│   │   ├── FirebaseSyncRequest.java       # { firebaseIdToken, nombre, ... }
│   │   └── UserResponse.java              # Usuario sin password
│   │
│   ├── 📁 security/                        # 🔒 SEGURIDAD
│   │   ├── JwtAuthenticationFilter.java   # Intercepta peticiones, valida JWT
│   │   └── JwtTokenProvider.java          # Genera y valida tokens JWT
│   │
│   └── 📁 exception/                       # ❌ MANEJO DE ERRORES
│       ├── GlobalExceptionHandler.java    # Captura excepciones, responde JSON
│       ├── ResourceNotFoundException.java # 404 - recurso no encontrado
│       └── BadRequestException.java       # 400 - petición inválida
│
├── 📁 src/main/resources/
│   ├── application.properties             # Config general
│   ├── application-dev.properties         # Config desarrollo (H2)
│   ├── application-prod.properties        # Config producción (MySQL)
│   └── 📁 db/migration/                   # 🔄 MIGRACIONES FLYWAY
│       ├── V1__Initial_schema.sql         # Crear tablas iniciales
│       ├── V2__Insert_sample_data.sql     # Datos de ejemplo (productos)
│       ├── V3__Add_order_delivery_fields.sql
│       ├── V4__Create_documentos_table.sql
│       ├── V5__Add_firebase_uid_column.sql
│       └── V6__Make_direccion_telefono_nullable.sql
│
├── 📁 docs/                                # 📚 DOCUMENTACIÓN
│   ├── FIREBASE_SETUP.md
│   ├── PROMPT_DESARROLLADOR_KOTLIN.md
│   └── scripts/                           # Scripts de utilidad
│
└── 📁 target/                              # 📦 COMPILADO
    └── huertohogar-api-0.0.1-SNAPSHOT.jar # JAR ejecutable
```

### Flujo de una petición:

```
📱 App Móvil
     │
     │ POST /api/v1/orders
     │ Header: Authorization: Bearer <JWT>
     ▼
┌─────────────────────────────────────────────────────────────┐
│                    SecurityConfig                           │
│  - Verifica si ruta es pública o requiere auth              │
│  - Si requiere auth → JwtAuthenticationFilter               │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│               JwtAuthenticationFilter                        │
│  - Extrae JWT del header                                    │
│  - Valida firma y expiración                                │
│  - Extrae email del usuario                                 │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│                   OrderController                            │
│  @PostMapping("/orders")                                    │
│  createOrder(@RequestBody OrderRequest request)             │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│                    OrderService                              │
│  - Valida datos del pedido                                  │
│  - Verifica stock de productos                              │
│  - Calcula total                                            │
│  - Crea entidad Order                                       │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│                   OrderRepository                            │
│  - orderRepository.save(order)                              │
│  - JPA genera INSERT en MySQL                               │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│                FirestoreSyncService                          │
│  - Sincroniza pedido a Firestore (async)                    │
│  - App admin ve el pedido en tiempo real                    │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
📱 App recibe: { id: 123, estado: "PENDIENTE", total: 15000 }
```

---

## 📝 RESUMEN PARA EL PROFESOR

**Tecnologías utilizadas:**
- **Backend**: Spring Boot 3.4, Java 17, MySQL, JWT, Firebase Admin SDK
- **Frontend**: Kotlin, Jetpack Compose, Retrofit, Room, DataStore
- **Cloud**: AWS EC2, Firebase Auth, Firebase Firestore
- **DevOps**: Docker, Flyway migrations

**Características principales:**
1. ✅ API RESTful completa con CRUD
2. ✅ Autenticación híbrida (Firebase + JWT propio)
3. ✅ Sincronización tiempo real con Firestore
4. ✅ Almacenamiento local (Room) + remoto (MySQL)
5. ✅ Desplegado en producción (EC2: 52.2.172.54:8080)

**Credenciales de prueba:**
- **Admin**: `profesor@huertohogar.cl` / `Admin123!`
- **Swagger**: http://52.2.172.54:8080/swagger-ui/index.html
