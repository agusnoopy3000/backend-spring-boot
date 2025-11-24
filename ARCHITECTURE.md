# Arquitectura del Proyecto Huerto Hogar API

## Visión General

Este proyecto implementa una API RESTful para el sistema Huerto Hogar, diseñada para soportar tanto clientes móviles (Kotlin) como clientes web (React). La arquitectura sigue los principios de diseño de Spring Boot con una estructura modular, escalable y fácil de mantener.

## Arquitectura de Capas

```
┌─────────────────────────────────────────────────┐
│           Clientes (React / Kotlin)             │
└─────────────────────────────────────────────────┘
                      ↓ HTTP/REST
┌─────────────────────────────────────────────────┐
│              Security Layer (JWT)               │
│  - JwtAuthenticationFilter                      │
│  - SecurityConfig                               │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│            Controller Layer (REST)              │
│  - AuthController                               │
│  - ProductController                            │
│  - UserController                               │
│  - OrderController                              │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│              Service Layer                      │
│  - AuthService                                  │
│  - ProductService                               │
│  - UserService                                  │
│  - OrderService                                 │
│  - JwtService                                   │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│            Repository Layer (JPA)               │
│  - UserRepository                               │
│  - ProductRepository                            │
│  - OrderRepository                              │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│         Database (MySQL / H2)                   │
└─────────────────────────────────────────────────┘
```

## Estructura de Directorios

```
src/main/java/cl/huertohogar/huertohogar_api/
├── config/                 # Configuraciones de Spring
│   ├── ApplicationConfig.java
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/            # Controladores REST
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── UserController.java
│   └── OrderController.java
├── dto/                   # Data Transfer Objects
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── OrderRequest.java
│   ├── OrderResponse.java
│   ├── OrderItemRequest.java
│   ├── OrderItemResponse.java
│   ├── UserResponse.java
│   └── ...
├── exception/             # Manejo de excepciones
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
├── model/                 # Entidades JPA
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── ...
├── repository/            # Repositorios JPA
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   └── OrderRepository.java
├── security/              # Componentes de seguridad
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
├── service/               # Lógica de negocio
│   ├── AuthService.java
│   ├── UserService.java
│   ├── ProductService.java
│   ├── OrderService.java
│   └── JwtServiceImpl.java
└── HuertohogarApiApplication.java
```

## Componentes Principales

### 1. Capa de Seguridad (Security Layer)

**Responsabilidades:**
- Autenticación y autorización mediante JWT
- Validación de tokens en cada request
- Control de acceso basado en roles (USER, ADMIN)

**Componentes:**
- `SecurityConfig`: Configura Spring Security, endpoints públicos/privados, CORS
- `JwtAuthenticationFilter`: Intercepta requests y valida tokens JWT
- `JwtService`: Genera y valida tokens JWT

**Flujo de Autenticación:**
1. Usuario se registra o inicia sesión en `/auth/register` o `/auth/login`
2. Sistema valida credenciales y genera token JWT
3. Cliente incluye token en header `Authorization: Bearer <token>`
4. `JwtAuthenticationFilter` valida el token en cada request
5. Si es válido, extrae email y rol del usuario

### 2. Capa de Controladores (Controller Layer)

**Responsabilidades:**
- Exponer endpoints REST
- Validar datos de entrada
- Orquestar llamadas a servicios
- Retornar respuestas HTTP

**Endpoints Principales:**

#### AuthController (`/auth`)
- `POST /auth/register` - Registrar nuevo usuario
- `POST /auth/login` - Iniciar sesión

#### ProductController (`/products`)
- `GET /products` - Listar productos (público con auth)
- `GET /products/{id}` - Obtener producto por ID
- `POST /products` - Crear producto (ADMIN)
- `PUT /products/{id}` - Actualizar producto (ADMIN)
- `DELETE /products/{id}` - Eliminar producto (ADMIN)

#### UserController (`/users`)
- `GET /users/me` - Perfil del usuario actual
- `GET /users` - Listar usuarios (ADMIN)
- `GET /users/{email}` - Obtener usuario por email (ADMIN)

#### OrderController (`/orders`)
- `POST /orders` - Crear pedido
- `GET /orders` - Listar pedidos del usuario
- `GET /orders/{id}` - Obtener pedido por ID
- `PUT /orders/{id}/status` - Actualizar estado (ADMIN)

### 3. Capa de Servicios (Service Layer)

**Responsabilidades:**
- Lógica de negocio
- Validaciones complejas
- Transacciones
- Orquestación entre repositorios

**Servicios:**
- `AuthService`: Registro, login, encriptación de passwords
- `UserService`: Gestión de usuarios
- `ProductService`: Gestión de productos, búsquedas
- `OrderService`: Gestión de pedidos, cálculos de totales
- `JwtService`: Generación y validación de tokens

### 4. Capa de Repositorios (Repository Layer)

**Responsabilidades:**
- Acceso a datos mediante JPA
- Queries personalizadas
- Abstracción de la base de datos

**Repositorios:**
- `UserRepository`: CRUD usuarios
- `ProductRepository`: CRUD productos + búsqueda
- `OrderRepository`: CRUD pedidos + consultas por usuario

### 5. Modelos de Datos (Model Layer)

**Entidades Principales:**

#### User
```java
- email (PK)
- nombre
- apellido
- password (encriptado)
- direccion
- telefono
- rol (USER/ADMIN)
- createdAt
```

#### Product
```java
- id (PK)
- codigo (unique)
- nombre
- descripcion
- precio
- stock
- imagen (URL)
- categoria
```

#### Order
```java
- id (PK)
- user (FK)
- items (OneToMany)
- total
- estado (PENDIENTE/CONFIRMADO/ENVIADO/ENTREGADO/CANCELADO)
- createdAt
```

#### OrderItem
```java
- id (PK)
- order (FK)
- productoId
- cantidad
- precioUnitario
```

## Flujos de Datos

### Flujo de Registro de Usuario

```
Cliente → POST /auth/register
         ↓
AuthController.register()
         ↓
AuthService.register()
    - Validar email único
    - Encriptar password
    - Crear User con rol USER
    - Guardar en BD
    - Generar JWT token
         ↓
← AuthResponse { token, user }
```

### Flujo de Creación de Pedido

```
Cliente → POST /orders
         ↓
JwtAuthenticationFilter
    - Validar token
    - Extraer email del token
         ↓
OrderController.createOrder()
         ↓
OrderService.createOrder()
    - Obtener User por email
    - Validar items
    - Calcular total
    - Crear Order con estado PENDIENTE
    - Guardar en BD
         ↓
← OrderResponse
```

### Flujo de Actualización de Producto (Admin)

```
Cliente → PUT /products/{id}
         ↓
JwtAuthenticationFilter
    - Validar token
    - Extraer rol del token
         ↓
@PreAuthorize("hasRole('ADMIN')")
    - Validar rol ADMIN
         ↓
ProductController.updateProduct()
         ↓
ProductService.updateProduct()
    - Buscar producto por ID
    - Actualizar campos
    - Guardar en BD
         ↓
← ProductResponse
```

## Seguridad

### Autenticación JWT

- **Algoritmo:** HS256 (HMAC with SHA-256)
- **Secret:** Configurado en `application.properties` (jwt.secret)
- **Expiración:** 24 horas (86400000 ms)
- **Claims incluidos:**
  - `sub`: Email del usuario
  - `role`: Rol del usuario (USER/ADMIN)
  - `iat`: Timestamp de emisión
  - `exp`: Timestamp de expiración

### Control de Acceso

**Endpoints Públicos:**
- `/auth/**` - Registro y login
- `/swagger-ui/**` - Documentación API
- `/v3/api-docs/**` - OpenAPI spec
- `/h2-console/**` - Consola H2 (solo desarrollo)

**Endpoints Autenticados:**
- Todos los demás requieren token JWT válido

**Endpoints de Administrador:**
- `POST /products` - Crear producto
- `PUT /products/{id}` - Actualizar producto
- `DELETE /products/{id}` - Eliminar producto
- `GET /users` - Listar usuarios
- `GET /users/{email}` - Ver usuario específico
- `PUT /orders/{id}/status` - Actualizar estado de pedido

### CORS

Configurado para permitir:
- **Origins:** Configurable en `application.properties` (cors.allowed-origins)
- **Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Headers:** Todos (*)
- **Credentials:** Habilitado

## Persistencia de Datos

### Desarrollo
- **Database:** H2 (en memoria)
- **DDL Auto:** create-drop
- **URL:** jdbc:h2:mem:testdb
- **Consola:** http://localhost:8080/h2-console

### Producción
- **Database:** MySQL 8.0
- **DDL Auto:** update
- **Flyway:** Deshabilitado
- **Conexión:** Via JDBC_URL, DB_USERNAME, DB_PASSWORD

## Validaciones

### Bean Validation (Jakarta Validation)

Todas las requests son validadas automáticamente usando anotaciones:

- `@NotBlank`: Campo no puede estar vacío
- `@NotNull`: Campo no puede ser null
- `@Email`: Formato de email válido
- `@Positive`: Número debe ser positivo
- `@Size(min=6)`: Tamaño mínimo
- `@NotEmpty`: Lista no puede estar vacía

### Validaciones Personalizadas

Implementadas en la capa de servicio:
- Código de producto único
- Email de usuario único
- Stock suficiente para pedidos
- Propiedad de pedidos (usuario vs admin)

## Documentación API

### Swagger/OpenAPI

- **URL:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs

**Características:**
- Descripción detallada de todos los endpoints
- Ejemplos de requests y responses
- Esquemas de DTOs con validaciones
- Integración con autenticación JWT
- Try-it-out funcional

## Escalabilidad

### Consideraciones de Diseño

1. **Stateless:** La aplicación no mantiene estado de sesión (JWT)
2. **Horizontal Scaling:** Puede ejecutarse en múltiples instancias
3. **Database Connection Pooling:** HikariCP optimiza conexiones
4. **Caching:** Preparado para agregar Redis/Memcached
5. **Async Processing:** Preparado para agregar mensajería (RabbitMQ/Kafka)

### Puntos de Extensión

- **Nuevas Entidades:** Agregar en package `model`, crear repository, service y controller
- **Nuevos Endpoints:** Agregar métodos en controllers existentes
- **Storage de Imágenes:** Integrar AWS S3 (ya existe DocumentoService como ejemplo)
- **Notificaciones:** Agregar servicio de email/SMS
- **Búsqueda Avanzada:** Integrar Elasticsearch
- **Caché:** Agregar Spring Cache + Redis

## Deployment

### Opciones de Despliegue

1. **Amazon EC2** (Ver README.md)
   - Instancia EC2 con Java 17
   - MySQL en Amazon RDS
   - Docker o deployment directo

2. **Docker Container**
   - Build: `docker build -t huertohogar-api .`
   - Run: `docker run -p 8080:8080 huertohogar-api`

3. **Cloud Platforms**
   - AWS Elastic Beanstalk
   - Google Cloud Run
   - Heroku
   - Azure App Service

### Health Checks

- **Endpoint:** `/actuator/health`
- **Response:** `{ "status": "UP" }`

## Monitoreo

### Spring Boot Actuator

Endpoints expuestos:
- `/actuator/health` - Estado de la aplicación
- Configurar más endpoints según necesidades en producción

### Logging

- **Framework:** SLF4J + Logback
- **Nivel:** DEBUG en desarrollo, INFO en producción
- **Pattern:** Timestamp, Level, Logger, Message

## Testing

### Estructura de Tests

```
src/test/java/
├── controller/
│   └── AuthControllerTest.java
├── service/
│   └── AuthServiceTest.java
└── HuertohogarApiApplicationTests.java
```

### Estrategia de Testing

- **Unit Tests:** Services con mocks
- **Integration Tests:** Controllers con MockMvc
- **Database Tests:** H2 in-memory

### Ejecutar Tests

```bash
mvn test
```

## Mejores Prácticas

1. **DTOs separados:** Request y Response DTOs para control fino
2. **Exception Handling:** GlobalExceptionHandler centraliza manejo de errores
3. **Validation:** Validación declarativa con Bean Validation
4. **Security:** JWT tokens, roles, HTTPS en producción
5. **Documentation:** Swagger para docs interactivas
6. **Separation of Concerns:** Capas bien definidas
7. **Dependency Injection:** Constructor injection con Lombok
8. **Transactions:** @Transactional en operaciones críticas

## Troubleshooting

### Problemas Comunes

1. **Token expirado:** El token JWT expira en 24h, re-autenticar
2. **403 Forbidden:** Verificar rol del usuario para endpoint
3. **CORS errors:** Verificar configuración de allowed-origins
4. **Database connection:** Verificar credenciales y accesibilidad de BD
5. **Port already in use:** Cambiar puerto en application.properties

## Próximos Pasos

### Funcionalidades Sugeridas

- [ ] Recuperación de contraseña
- [ ] Confirmación de email
- [ ] Paginación en listado de productos
- [ ] Filtros avanzados de búsqueda
- [ ] Carrito de compras persistente
- [ ] Integración con pasarelas de pago
- [ ] Notificaciones push
- [ ] Panel de administración
- [ ] Analytics y reportes
- [ ] Integración con inventario físico
- [ ] Sistema de reviews y ratings
- [ ] Programa de fidelidad

## Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
