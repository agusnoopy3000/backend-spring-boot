# Guía de Alineación Frontend-Backend - Huerto Hogar API

## Resumen Ejecutivo

Este documento proporciona toda la información necesaria para alinear correctamente las aplicaciones frontend (React y Kotlin) con el backend Spring Boot.

---

## 1. Backend con BD, Modelos y Lógica de Negocio

### 1.1 Conexión a Base de Datos

**Desarrollo (H2 en memoria):**
```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Producción (MySQL):**
```properties
# application-prod.properties
spring.datasource.url=${JDBC_URL:jdbc:mysql://localhost:3306/huertohogar}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

**Explicación:**
- **Desarrollo**: Usamos H2 en memoria para rapidez en pruebas y desarrollo local
- **Producción**: MySQL para persistencia real con variables de entorno para seguridad
- `ddl-auto`: `create-drop` en dev (recrea tablas cada vez), `update` en prod (actualiza schema sin perder datos)

### 1.2 Modelos de Datos (Entidades JPA)

#### User (Usuario)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "email", unique = true, nullable = false)
    private String email;           // PK: email único
    
    private String nombre;          // Nombre del usuario
    private String apellido;        // Apellido
    private String password;        // Password encriptado con BCrypt
    private String direccion;       // Dirección de entrega
    private String telefono;        // Teléfono de contacto
    private String rol;             // Rol: "user" o "admin"
    
    @CreationTimestamp
    private LocalDateTime createdAt; // Fecha de registro
}
```

**Campos clave para frontend:**
- `email`: Usado como identificador único
- `rol`: Determina permisos (user/admin)
- `password`: NUNCA se devuelve en responses

#### Product (Producto)
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // PK auto-generado
    
    @Column(unique = true)
    private String codigo;          // Código único del producto
    private String nombre;          // Nombre del producto
    private String descripcion;     // Descripción
    private Double precio;          // Precio en pesos chilenos
    private Integer stock;          // Cantidad disponible
    private String imagen;          // URL de la imagen
    private String categoria;       // Categoría (ej: "Verduras")
}
```

**Campos clave para frontend:**
- `id`: Para operaciones CRUD
- `codigo`: Identificador de negocio único
- `stock`: Para validar disponibilidad
- `imagen`: URL completa de la imagen del producto

#### Order (Pedido)
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_email")
    private User user;              // Usuario que realiza el pedido
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;  // Items del pedido
    
    private Double total;           // Total calculado
    
    @Enumerated(EnumType.STRING)
    private Estado estado;          // PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum Estado {
        PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
    }
}
```

#### OrderItem (Item de Pedido)
```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productoId;      // Código del producto
    private Integer cantidad;       // Cantidad ordenada
    private Double precioUnitario;  // Precio al momento del pedido
}
```

**Relaciones importantes:**
- Un `User` puede tener muchos `Order`
- Un `Order` tiene muchos `OrderItem`
- El `total` se calcula: `sum(item.cantidad * item.precioUnitario)`

### 1.3 Lógica de Negocio (Services)

#### AuthService
**Responsabilidades:**
1. **Registrar usuario** (`register`):
   - Valida que el email no exista
   - Encripta password con BCrypt
   - Asigna rol "user" por defecto
   - Genera token JWT
   - Guarda usuario en BD

2. **Login** (`login`):
   - Valida email y password
   - Verifica password con BCrypt
   - Genera token JWT con claims (email, rol)
   - Retorna token y datos de usuario

**Endpoints asociados:**
- `POST /auth/register`
- `POST /auth/login`

#### ProductService
**Responsabilidades:**
1. **Crear producto** (`createProduct`):
   - Valida código único
   - Guarda producto en BD
   
2. **Listar productos** (`getAllProducts`):
   - Opcionalmente filtra por búsqueda (query `q`)
   - Busca en nombre y descripción

3. **Obtener por ID** (`getProductById`)
4. **Actualizar** (`updateProduct`)
5. **Eliminar** (`deleteProduct`)

**Validaciones:**
- Código de producto único
- Stock y precio positivos
- Campos requeridos

**Endpoints asociados:**
- `GET /products` (con query param `q` opcional)
- `GET /products/{id}`
- `POST /products` (Admin)
- `PUT /products/{id}` (Admin)
- `DELETE /products/{id}` (Admin)

#### OrderService
**Responsabilidades:**
1. **Crear pedido** (`createOrder`):
   - Obtiene usuario por email (del token JWT)
   - Valida items del pedido
   - Calcula total: `sum(item.cantidad * item.precioUnitario)`
   - Crea orden con estado PENDIENTE
   - Guarda en BD

2. **Listar pedidos** (`getOrdersByUser`):
   - Retorna pedidos del usuario autenticado
   - Admin puede ver pedidos de cualquier usuario

3. **Actualizar estado** (`updateOrderStatus`):
   - Solo admin puede cambiar estado
   - Estados: PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO
   - También puede cancelarse: CANCELADO

**Endpoints asociados:**
- `POST /orders`
- `GET /orders` (con query param `userEmail` para admin)
- `GET /orders/{id}`
- `PUT /orders/{id}/status` (Admin)

#### UserService
**Responsabilidades:**
1. **Obtener perfil** (`getUserById`)
2. **Listar usuarios** (`getAllUsers`) - Solo admin
3. **Obtener por email** - Solo admin

**Endpoints asociados:**
- `GET /users/me`
- `GET /users` (Admin)
- `GET /users/{email}` (Admin)

---

## 2. API REST con Spring Boot + Swagger (CRUD)

### 2.1 Estructura de Controladores

Todos los controladores tienen:
- Anotación `@RestController`
- Path base con `@RequestMapping`
- Documentación Swagger con `@Tag`, `@Operation`, `@ApiResponse`
- Validación de entrada con `@Valid`
- Manejo de errores centralizado

### 2.2 Endpoints Disponibles

#### AuthController (`/auth`)

**1. Registro de Usuario**
```
POST /auth/register
Content-Type: application/json

Request Body:
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "password": "MyS3cur3P@ssw0rd!",
  "direccion": "Av. Principal 123",
  "telefono": "+56912345678"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "email": "juan@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "direccion": "Av. Principal 123",
    "telefono": "+56912345678",
    "rol": "user"
  }
}
```

**2. Login**
```
POST /auth/login
Content-Type: application/json

Request Body:
{
  "email": "juan@example.com",
  "password": "MyS3cur3P@ssw0rd!"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { ... }
}
```

#### ProductController (`/products`)

**1. Listar Productos**
```
GET /products
GET /products?q=tomate    (búsqueda)
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 1,
    "codigo": "VRD-001",
    "nombre": "Tomate Cherry",
    "descripcion": "Tomates frescos",
    "precio": 2500.0,
    "stock": 100,
    "imagen": "https://...",
    "categoria": "Verduras"
  }
]
```

**2. Obtener Producto por ID**
```
GET /products/{id}
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "codigo": "VRD-001",
  ...
}
```

**3. Crear Producto (Admin)**
```
POST /products
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "codigo": "VRD-003",
  "nombre": "Zanahoria",
  "descripcion": "Zanahoria orgánica",
  "precio": 1500.0,
  "stock": 200,
  "imagen": "https://...",
  "categoria": "Verduras"
}

Response (200 OK):
{
  "id": 3,
  ...
}
```

**4. Actualizar Producto (Admin)**
```
PUT /products/{id}
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "codigo": "VRD-003",
  "nombre": "Zanahoria Premium",
  "descripcion": "Zanahoria orgánica premium",
  "precio": 1800.0,
  "stock": 180,
  "imagen": "https://...",
  "categoria": "Verduras"
}
```

**5. Eliminar Producto (Admin)**
```
DELETE /products/{id}
Authorization: Bearer {token}

Response (204 No Content)
```

#### OrderController (`/orders`)

**1. Crear Pedido**
```
POST /orders
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "items": [
    {
      "productoId": "VRD-001",
      "cantidad": 5,
      "precioUnitario": 2500.0
    },
    {
      "productoId": "VRD-002",
      "cantidad": 3,
      "precioUnitario": 1800.0
    }
  ]
}

Response (200 OK):
{
  "id": 1,
  "user": {
    "email": "juan@example.com",
    "nombre": "Juan",
    ...
  },
  "items": [ ... ],
  "total": 17900.0,
  "estado": "PENDIENTE",
  "createdAt": "2025-11-24T10:30:00"
}
```

**2. Listar Pedidos del Usuario**
```
GET /orders
Authorization: Bearer {token}

Response (200 OK):
[ { "id": 1, ... } ]
```

**3. Obtener Pedido por ID**
```
GET /orders/{id}
Authorization: Bearer {token}

Response (200 OK):
{ "id": 1, ... }
```

**4. Actualizar Estado (Admin)**
```
PUT /orders/{id}/status
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "estado": "CONFIRMADO"
}

Valores posibles: PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
```

#### UserController (`/users`)

**1. Perfil Actual**
```
GET /users/me
Authorization: Bearer {token}

Response (200 OK):
{
  "email": "juan@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "direccion": "Av. Principal 123",
  "telefono": "+56912345678",
  "rol": "user"
}
```

**2. Listar Usuarios (Admin)**
```
GET /users?page=0&size=10
Authorization: Bearer {token}

Response (200 OK):
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 10,
  "totalPages": 1
}
```

---

## 3. Integración Backend-Frontend

### 3.1 CORS Configuration

**Configuración actual:**
```properties
cors.allowed-origins=http://localhost:5173,https://your-frontend-domain.com
```

**Para actualizar:**
1. Desarrollo React: Agregar `http://localhost:3000` o `http://localhost:5173`
2. Desarrollo Kotlin: Permitir todas las IPs locales o usar `*` en dev
3. Producción: URLs específicas de frontend desplegado

**Editar en `application.properties`:**
```properties
cors.allowed-origins=http://localhost:5173,http://localhost:3000,http://10.0.2.2:8080
```

### 3.2 URLs Base por Entorno

**Desarrollo Local:**
- Backend: `http://localhost:8080`
- React: `http://localhost:5173` o `http://localhost:3000`
- Kotlin (Emulador): `http://10.0.2.2:8080`
- Kotlin (Dispositivo): `http://{IP_DEL_PC}:8080`

**Producción:**
- Backend: `https://api.huertohogar.cl`
- Frontend: `https://huertohogar.cl`

### 3.3 Formato de Comunicación

**Headers Requeridos:**
```
Content-Type: application/json
Authorization: Bearer {token}    (excepto /auth/register y /auth/login)
```

**Manejo de Errores:**
```json
// 400 Bad Request
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/products"
}

// 401 Unauthorized
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado",
  "path": "/orders"
}

// 404 Not Found
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found",
  "path": "/products/999"
}
```

---

## 4. Autenticación JWT con Roles

### 4.1 Flujo de Autenticación

1. **Usuario se registra o inicia sesión** → Recibe token JWT
2. **Cliente guarda token** (localStorage/sessionStorage en web, SharedPreferences en Android)
3. **Cliente incluye token en cada request** → Header: `Authorization: Bearer {token}`
4. **Backend valida token** → Extrae email y rol
5. **Backend autoriza operación** → Verifica permisos según rol

### 4.2 Estructura del Token JWT

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "juan@example.com",    // Email del usuario
  "role": "user",                // Rol: user o admin
  "iat": 1700000000,             // Timestamp de emisión
  "exp": 1700086400              // Timestamp de expiración (24h)
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

### 4.3 Roles y Permisos

**Usuario Normal (USER):**
- ✅ Ver productos
- ✅ Crear pedidos
- ✅ Ver sus propios pedidos
- ✅ Ver su perfil
- ❌ Crear/editar/eliminar productos
- ❌ Ver otros usuarios
- ❌ Cambiar estado de pedidos

**Administrador (ADMIN):**
- ✅ Todas las operaciones de USER
- ✅ Crear/editar/eliminar productos
- ✅ Ver todos los usuarios
- ✅ Ver todos los pedidos
- ✅ Cambiar estado de pedidos

### 4.4 Implementación en Frontend

**React (Axios):**
```javascript
// Guardar token después del login
localStorage.setItem('token', response.data.token);

// Configurar interceptor
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Manejar expiración
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

**Kotlin (Retrofit):**
```kotlin
// Guardar token
val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
sharedPreferences.edit().putString("jwt_token", token).apply()

// Interceptor
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sharedPreferences.getString("jwt_token", null)
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

// Configurar OkHttpClient
val client = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor())
    .build()
```

---

## 5. Gestión de Sesiones en Frontend

### 5.1 Persistencia del Token

**Web (React):**
```javascript
// Al hacer login exitoso
const login = async (email, password) => {
  const response = await authService.login(email, password);
  localStorage.setItem('token', response.token);
  localStorage.setItem('user', JSON.stringify(response.user));
};

// Verificar sesión al cargar app
useEffect(() => {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  if (token && user) {
    setIsAuthenticated(true);
    setUser(JSON.parse(user));
  }
}, []);

// Logout
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  setIsAuthenticated(false);
};
```

**Móvil (Kotlin):**
```kotlin
// Al hacer login exitoso
viewModelScope.launch {
    val response = authRepository.login(email, password)
    sharedPreferences.edit().apply {
        putString("jwt_token", response.token)
        putString("user_email", response.user.email)
        putString("user_role", response.user.rol)
        apply()
    }
}

// Verificar sesión
fun isLoggedIn(): Boolean {
    return sharedPreferences.getString("jwt_token", null) != null
}

// Logout
fun logout() {
    sharedPreferences.edit().clear().apply()
}
```

### 5.2 Manejo de Expiración

El token expira en 24 horas. Opciones:

1. **Redirect al login cuando expire:**
   - Interceptor detecta 401
   - Limpia token
   - Redirige a login

2. **Refresh token (futuro):**
   - Implementar endpoint `/auth/refresh`
   - Renovar token antes de expiración

---

## 6. Restricciones de Acceso en Frontend

### 6.1 Rutas Protegidas (React)

```javascript
// ProtectedRoute.jsx
const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  
  if (!token) {
    return <Navigate to="/login" />;
  }
  
  if (requireAdmin && user.rol !== 'admin') {
    return <Navigate to="/forbidden" />;
  }
  
  return children;
};

// App.jsx
<Routes>
  <Route path="/login" element={<Login />} />
  <Route path="/products" element={
    <ProtectedRoute>
      <ProductList />
    </ProtectedRoute>
  } />
  <Route path="/admin/products" element={
    <ProtectedRoute requireAdmin>
      <ProductAdmin />
    </ProtectedRoute>
  } />
</Routes>
```

### 6.2 Componentes Condicionales (React)

```javascript
const ProductCard = ({ product }) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.rol === 'admin';
  
  return (
    <div>
      <h3>{product.nombre}</h3>
      <p>{product.descripcion}</p>
      <p>${product.precio}</p>
      
      {/* Botones visibles solo para admin */}
      {isAdmin && (
        <>
          <button onClick={() => handleEdit(product.id)}>Editar</button>
          <button onClick={() => handleDelete(product.id)}>Eliminar</button>
        </>
      )}
    </div>
  );
};
```

### 6.3 Navegación Condicional (Kotlin)

```kotlin
// Composable con restricción
@Composable
fun AdminProductScreen(navController: NavController) {
    val user = getUserFromPreferences()
    
    if (user.rol != "admin") {
        LaunchedEffect(Unit) {
            navController.navigate("forbidden")
        }
        return
    }
    
    // UI de administración
    Column {
        Button(onClick = { /* crear producto */ }) {
            Text("Nuevo Producto")
        }
        // ...
    }
}

// Botones condicionales
@Composable
fun ProductCard(product: Product) {
    val user = getUserFromPreferences()
    
    Card {
        Column {
            Text(product.nombre)
            Text("$${product.precio}")
            
            if (user.rol == "admin") {
                Row {
                    Button(onClick = { /* editar */ }) { Text("Editar") }
                    Button(onClick = { /* eliminar */ }) { Text("Eliminar") }
                }
            }
        }
    }
}
```

---

## 7. Swagger UI - Documentación Interactiva

### 7.1 Acceso a Swagger

**URL:** `http://localhost:8080/swagger-ui/index.html`

**Características:**
- Documentación completa de todos los endpoints
- Ejemplos de request/response
- Try-it-out para probar endpoints
- Esquemas de DTOs con validaciones

### 7.2 Probar Endpoints con Swagger

1. **Abrir Swagger UI**
2. **Registrar usuario:**
   - Expandir `POST /auth/register`
   - Click "Try it out"
   - Completar JSON de ejemplo
   - Click "Execute"
   - Copiar token del response

3. **Autenticar en Swagger:**
   - Click botón "Authorize" (arriba a la derecha)
   - Pegar: `Bearer {token}`
   - Click "Authorize"

4. **Probar endpoints protegidos:**
   - Ahora todos los requests incluirán el token automáticamente

### 7.3 OpenAPI Spec

**URL JSON:** `http://localhost:8080/v3/api-docs`

Usar para:
- Generar clientes automáticamente (Swagger Codegen)
- Importar a Postman
- Documentación externa

---

## 8. Validaciones y Mensajes de Error

### 8.1 Validaciones en Request

**ProductRequest:**
- `codigo`: No vacío, único
- `nombre`: No vacío
- `precio`: No nulo, positivo
- `stock`: No nulo, positivo

**RegisterRequest:**
- `nombre`: No vacío
- `apellido`: No vacío
- `email`: No vacío, formato email válido, único
- `password`: No vacío, mínimo 6 caracteres
- `direccion`: No vacía
- `telefono`: No vacío

**OrderRequest:**
- `items`: No vacío, al menos un item
- `items[].productoId`: No nulo
- `items[].cantidad`: No nulo, positivo
- `items[].precioUnitario`: No nulo

### 8.2 Respuestas de Error

**400 Bad Request - Validación fallida:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Product code already exists"
}
```

**401 Unauthorized - Token inválido:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado"
}
```

**403 Forbidden - Sin permisos:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Acceso denegado"
}
```

**404 Not Found - Recurso no existe:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found"
}
```

---

## 9. Checklist de Integración

### Para Frontend React:

- [ ] Configurar axios con base URL correcta
- [ ] Implementar interceptor para agregar token JWT
- [ ] Guardar token en localStorage al login
- [ ] Implementar manejo de 401 (redirect a login)
- [ ] Crear rutas protegidas con ProtectedRoute
- [ ] Implementar componentes condicionales según rol
- [ ] Validar formularios antes de enviar
- [ ] Mostrar mensajes de error del backend
- [ ] Implementar loading states
- [ ] Probar con Swagger primero

### Para Frontend Kotlin:

- [ ] Configurar Retrofit con base URL correcta
- [ ] Usar `http://10.0.2.2:8080` para emulador
- [ ] Implementar interceptor para agregar token JWT
- [ ] Guardar token en SharedPreferences al login
- [ ] Implementar manejo de 401 en interceptor
- [ ] Validar permisos antes de mostrar pantallas admin
- [ ] Implementar componentes condicionales según rol
- [ ] Validar formularios en ViewModels
- [ ] Mostrar mensajes de error del backend
- [ ] Implementar estados de carga (Loading, Success, Error)
- [ ] Probar con Swagger primero

### Para Backend:

- [x] Base de datos configurada (H2 dev, MySQL prod)
- [x] Modelos de datos creados
- [x] Servicios con lógica de negocio
- [x] Controladores REST implementados
- [x] Swagger configurado
- [x] JWT implementado
- [x] CORS configurado
- [x] Validaciones implementadas
- [x] Manejo de errores global
- [x] Tests pasando

---

## 10. Usuarios de Prueba

### Usuario Admin
```
Email: admin@huertohogar.cl
Password: admin123
Rol: admin
```

### Usuario Normal
```
Email: cliente@demo.com
Password: password
Rol: user
```

---

## 11. Recursos Adicionales

### Documentación
- **ARCHITECTURE.md**: Arquitectura completa del sistema
- **API_EXAMPLES.md**: Ejemplos de código Kotlin y React
- **README.md**: Guía de despliegue
- **Swagger UI**: Documentación interactiva

### Colección Postman
- Archivo: `Huertohogar_API.postman_collection.json`
- Importar en Postman para probar todos los endpoints

### Scripts Útiles

**Iniciar backend en desarrollo:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Verificar que esté corriendo:**
```bash
curl http://localhost:8080/actuator/health
```

**Acceder a H2 Console:**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (dejar vacío)
```

---

## Resumen de URLs Importantes

| Servicio | Desarrollo | Producción |
|----------|-----------|------------|
| Backend API | http://localhost:8080 | https://api.huertohogar.cl |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | https://api.huertohogar.cl/swagger-ui/index.html |
| OpenAPI Spec | http://localhost:8080/v3/api-docs | https://api.huertohogar.cl/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console | N/A |
| Health Check | http://localhost:8080/actuator/health | https://api.huertohogar.cl/actuator/health |

---

## Contacto y Soporte

Para preguntas adicionales:
- Email: info@huertohogar.cl
- Repositorio: https://github.com/agusnoopy3000/backend-spring-boot
- Issues: https://github.com/agusnoopy3000/backend-spring-boot/issues
