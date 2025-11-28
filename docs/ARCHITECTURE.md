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
│  - ProductController (CRUD completo)            │
│  - UserController (CRUD completo) ⭐            │
│  - OrderController (+ cambio estado) ⭐         │
│  - DocumentoController (S3) ⭐                  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│              Service Layer                      │
│  - AuthService                                  │
│  - ProductService (CRUD completo)               │
│  - UserService (CRUD completo) ⭐               │
│  - OrderService (+ estados) ⭐                  │
│  - DocumentoService (metadatos) ⭐              │
│  - S3Service (AWS SDK) ⭐                       │
│  - JwtService                                   │
└─────────────────────────────────────────────────┘
         ↓                              ↓
┌─────────────────────────┐   ┌────────────────────┐
│   Repository Layer      │   │   AWS S3 Client    │
│   (JPA)                 │   │                    │
│  - UserRepository       │   │  - Upload files    │
│  - ProductRepository    │   │  - Delete files    │
│  - OrderRepository      │   │  - Bucket: huerto- │
│  - DocumentoRepository⭐│   │    hogar-documentos│
└─────────────────────────┘   └────────────────────┘
         ↓                              ↓
┌─────────────────────────┐   ┌────────────────────┐
│   Database (MySQL/H2)   │   │   Amazon S3        │
│                         │   │                    │
│  - users                │   │  /documentos/      │
│  - products             │   │    ├── 2024/11/    │
│  - orders               │   │    ├── 2024/12/    │
│  - order_items          │   │    └── 2025/01/    │
│  - documentos ⭐        │   │                    │
│  - flyway_history       │   │  (Archivos físicos)│
└─────────────────────────┘   └────────────────────┘
```

**Leyenda:**
- ⭐ = Nuevo o actualizado recientemente
- Las flechas muestran el flujo de datos
- Service Layer se comunica tanto con Repository (BD) como con S3 (archivos)

## Estructura de Directorios

```
backend-spring-boot-copilot-add-rest-api-endpoints/
├── src/
│   ├── main/
│   │   ├── java/cl/huertohogar/huertohogar_api/
│   │   │   ├── HuertohogarApiApplication.java     # Clase principal
│   │   │   ├── config/                            # Configuraciones Spring
│   │   │   │   ├── ApplicationConfig.java         # Beans de configuración
│   │   │   │   ├── DataSeeder.java               # ⭐ Carga datos iniciales
│   │   │   │   ├── SecurityConfig.java            # Seguridad y CORS
│   │   │   │   └── SwaggerConfig.java            # Documentación API
│   │   │   ├── controller/                        # Controladores REST
│   │   │   │   ├── AuthController.java           # Login y registro
│   │   │   │   ├── DocumentoController.java      # ⭐ Gestión documentos S3
│   │   │   │   ├── HelloController.java          # Test endpoint
│   │   │   │   ├── OrderController.java          # Gestión pedidos
│   │   │   │   ├── ProductController.java        # CRUD productos
│   │   │   │   └── UserController.java           # CRUD usuarios
│   │   │   ├── dto/                              # Data Transfer Objects
│   │   │   │   ├── AuthResponse.java            # Response login/registro
│   │   │   │   ├── LoginRequest.java            # Request login
│   │   │   │   ├── RegisterRequest.java         # Request registro
│   │   │   │   ├── ProductRequest.java          # Request crear/editar producto
│   │   │   │   ├── ProductResponse.java         # Response producto
│   │   │   │   ├── OrderRequest.java            # Request crear pedido
│   │   │   │   ├── OrderResponse.java           # Response pedido
│   │   │   │   ├── OrderItemRequest.java        # Item del pedido (request)
│   │   │   │   ├── OrderItemResponse.java       # Item del pedido (response)
│   │   │   │   └── UserResponse.java            # Response usuario
│   │   │   ├── exception/                        # Manejo de excepciones
│   │   │   │   ├── GlobalExceptionHandler.java  # Handler global
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── BadRequestException.java
│   │   │   ├── model/                            # Entidades JPA
│   │   │   │   ├── User.java                    # Usuario (PK: email)
│   │   │   │   ├── Product.java                 # Producto (PK: id)
│   │   │   │   ├── Order.java                   # Pedido (PK: id)
│   │   │   │   ├── OrderItem.java               # Item de pedido (PK: id)
│   │   │   │   ├── OrderStatus.java             # ⭐ Enum estados pedido
│   │   │   │   ├── Role.java                    # Enum roles (USER/ADMIN)
│   │   │   │   └── Documento.java               # ⭐ Metadatos archivos S3
│   │   │   ├── repository/                       # Repositorios JPA
│   │   │   │   ├── UserRepository.java          # CRUD usuarios
│   │   │   │   ├── ProductRepository.java       # CRUD productos
│   │   │   │   ├── OrderRepository.java         # CRUD pedidos
│   │   │   │   └── DocumentoRepository.java     # ⭐ CRUD documentos
│   │   │   ├── security/                         # Componentes seguridad
│   │   │   │   ├── JwtAuthenticationFilter.java # Filtro JWT
│   │   │   │   ├── JwtService.java              # Interface servicio JWT
│   │   │   │   └── CustomUserDetailsService.java # UserDetails de Spring
│   │   │   └── service/                          # Lógica de negocio
│   │   │       ├── AuthService.java             # Autenticación
│   │   │       ├── UserService.java             # Lógica usuarios
│   │   │       ├── ProductService.java          # Lógica productos
│   │   │       ├── OrderService.java            # Lógica pedidos
│   │   │       ├── DocumentoService.java        # ⭐ Lógica documentos
│   │   │       ├── S3Service.java               # ⭐ Cliente AWS S3
│   │   │       └── JwtServiceImpl.java          # Implementación JWT
│   │   └── resources/
│   │       ├── application.properties            # Config principal
│   │       ├── application-dev.properties       # Config desarrollo
│   │       ├── application-prod.properties      # Config producción
│   │       ├── data.sql                         # ⚠️ Deprecated (usar Flyway)
│   │       └── db/migration/                    # Migraciones Flyway
│   │           ├── V1__Initial_schema.sql       # Schema inicial
│   │           ├── V2__Insert_sample_data.sql   # Datos de prueba
│   │           ├── V3__Add_order_delivery_fields.sql # Campos entrega
│   │           └── V4__Create_documentos_table.sql   # ⭐ Tabla documentos
│   └── test/
│       └── java/cl/huertohogar/huertohogar_api/
│           ├── HuertohogarApiApplicationTests.java
│           ├── controller/
│           │   └── AuthControllerTest.java
│           └── service/
│               └── AuthServiceTest.java
├── target/                                      # Archivos compilados
│   ├── huertohogar-api-0.0.1-SNAPSHOT.jar      # JAR ejecutable
│   ├── classes/                                # .class compilados
│   └── test-classes/                           # Tests compilados
├── pom.xml                                     # Dependencias Maven
├── Dockerfile                                  # Imagen Docker
├── README.md                                   # Documentación principal
├── ARCHITECTURE.md                             # Este archivo
├── API_EXAMPLES.md                             # Ejemplos de uso API
├── PROJECT_RESUMEN.md                          # Resumen del proyecto
├── deploy-to-ec2.sh                           # ⭐ Script despliegue EC2
├── update-ec2.sh                              # ⭐ Script actualización rápida
├── remove_duplicate_flyway_migration.sh       # Script limpieza Flyway
├── mvnw                                       # Maven Wrapper (Linux/Mac)
├── mvnw.cmd                                   # Maven Wrapper (Windows)
└── .gitignore                                 # Archivos ignorados por Git
```

**Leyenda:**
- ⭐ = Nuevo o actualizado recientemente
- ⚠️ = Deprecated o en desuso
- 📁 = Directorio
- 📄 = Archivo

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
- `GET /orders` - Listar pedidos (del usuario o todos si es ADMIN)
- `GET /orders/{id}` - Obtener pedido por ID
- `PUT /orders/{id}/status` - Actualizar estado (ADMIN) ⭐ **NUEVO**

#### DocumentoController (`/documentos`)
- `POST /documentos` - Subir documento a S3 (ADMIN)
- `GET /documentos` - Listar documentos (ADMIN)
- `GET /documentos/mis-documentos` - Mis documentos
- `GET /documentos/{id}` - Obtener documento por ID (ADMIN)
- `DELETE /documentos/{id}` - Eliminar documento (ADMIN)

### 3. Capa de Servicios (Service Layer)

**Responsabilidades:**
- Lógica de negocio
- Validaciones complejas
- Transacciones
- Orquestación entre repositorios

**Servicios:**
- `AuthService`: Registro, login, encriptación de passwords
- `UserService`: Gestión de usuarios, CRUD completo
- `ProductService`: Gestión de productos, búsquedas, CRUD completo
- `OrderService`: Gestión de pedidos, cálculos de totales, actualización de estado
- `DocumentoService`: Gestión de documentos y metadatos
- `S3Service`: Integración con AWS S3 para almacenamiento de archivos
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
- `DocumentoRepository`: CRUD documentos + consultas por usuario

### 5. Modelos de Datos (Model Layer)

**Entidades Principales:**

#### User
```java
- email (PK)
- run (RUN/RUT chileno)
- nombre
- apellido
- password (encriptado con BCrypt)
- direccion
- telefono
- rol (USER/ADMIN)
- createdAt
```

#### Product
```java
- id (PK, auto-increment)
- codigo (unique) - Código de negocio (ej: VRD-001)
- nombre
- descripcion
- precio
- stock
- imagen (URL)
- categoria
```

#### Order
```java
- id (PK, auto-increment)
- user (FK a User.email)
- items (OneToMany a OrderItem)
- total
- estado (OrderStatus enum)
- direccionEntrega
- region
- comuna
- comentarios
- fechaEntrega
- createdAt
```

#### OrderItem
```java
- id (PK, auto-increment)
- order (FK a Order.id)
- productoId (String, referencia a Product.codigo)
- cantidad
- precioUnitario
```

#### OrderStatus (Enum)
```java
- PENDIENTE - Pedido recién creado
- CONFIRMADO - Confirmado por admin
- ENVIADO - En camino al cliente
- ENTREGADO - Completado (estado final)
- CANCELADO - Cancelado (estado final)
```

#### Documento
```java
- id (PK, auto-increment)
- nombre
- descripcion
- tipoArchivo (PDF, DOC, DOCX, XLS, XLSX, imagen)
- tamano (en bytes)
- urlS3 (URL en bucket S3)
- nombreArchivoS3
- uploadedBy (FK a User.email)
- createdAt
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

### Flujo de Actualización de Estado de Pedido (Admin) ⭐ **NUEVO**

```
Cliente → PUT /orders/{id}/status
         ↓
JwtAuthenticationFilter
    - Validar token JWT
    - Extraer rol del token
         ↓
OrderController.updateStatus()
    - Verificar rol ADMIN manualmente
         ↓
OrderService.updateOrderStatus()
    - Buscar pedido por ID
    - Validar estado no sea final (ENTREGADO/CANCELADO)
    - Actualizar estado
    - Guardar en BD
         ↓
← OrderResponse con nuevo estado
```

### Flujo de Subida de Documento a S3 (Admin) ⭐ **NUEVO**

```
Cliente → POST /documentos (multipart/form-data)
         ↓
JwtAuthenticationFilter
    - Validar token JWT
    - Extraer email del token
         ↓
@PreAuthorize("hasRole('ADMIN')")
    - Validar rol ADMIN
         ↓
DocumentoController.uploadDocumento()
         ↓
S3Service.uploadFile()
    - Validar tipo de archivo
    - Validar tamaño (max 10MB)
    - Generar nombre único con UUID
    - Organizar por fecha (YYYY/MM/)
    - Subir a S3
    - Retornar URL
         ↓
DocumentoService.saveDocumento()
    - Crear entidad Documento con metadatos
    - Guardar en BD
         ↓
← Documento con URL S3
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
- `POST /users` - Crear usuario desde panel admin
- `PUT /users/{email}` - Actualizar usuario
- `DELETE /users/{email}` - Eliminar usuario
- `GET /users` - Listar usuarios
- `GET /users/{email}` - Ver usuario específico
- `PUT /orders/{id}/status` - Actualizar estado de pedido ⭐
- `POST /documentos` - Subir documento a S3
- `GET /documentos` - Listar todos los documentos
- `DELETE /documentos/{id}` - Eliminar documento

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
- **DDL Auto:** validate (con Flyway para migraciones)
- **Flyway:** Habilitado para control de versiones de BD
- **Migraciones:**
  - V1: Schema inicial (users, products, orders, order_items)
  - V2: Datos de ejemplo
  - V3: Campos de entrega en orders
  - V4: Tabla documentos
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
- **Storage de Archivos:** ✅ Ya implementado con AWS S3 (S3Service + DocumentoService)
- **Notificaciones:** Agregar servicio de email/SMS
- **Búsqueda Avanzada:** Integrar Elasticsearch
- **Caché:** Agregar Spring Cache + Redis

## Integración con AWS

### AWS S3 (Storage de Archivos)

**Configuración:**
- Bucket: `huerto-hogar-documentos`
- Región: `us-east-1`
- Credenciales: Via variables de entorno `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY`

**Componentes:**
- `S3Service`: Cliente AWS SDK S3
  - `uploadFile()`: Sube archivos con validación de tipo y tamaño
  - `deleteFile()`: Elimina archivos del bucket
- `DocumentoService`: Lógica de negocio para documentos
- `Documento` entity: Metadatos en MySQL, archivos en S3

**Tipos de Archivo Permitidos:**
- Documentos: PDF, DOC, DOCX, XLS, XLSX
- Imágenes: PNG, JPG, JPEG
- Tamaño máximo: 10 MB

**Estructura en S3:**
```
s3://huerto-hogar-documentos/
└── documentos/
    ├── 2024/
    │   ├── 11/
    │   │   ├── uuid-documento1.pdf
    │   │   └── uuid-documento2.xlsx
    │   └── 12/
    │       └── uuid-documento3.jpg
    └── 2025/
        └── 01/
            └── uuid-documento4.docx
```

## Deployment

### Opciones de Despliegue

1. **Amazon EC2** (Ver README.md) ✅ **IMPLEMENTADO**
   - Instancia EC2: `52.2.172.54`
   - MySQL local en EC2
   - Java 17+ instalado
   - Deployment manual con JAR
   - Variables de entorno para credenciales

2. **Docker Container**
   - Build: `docker build -t huertohogar-api .`
   - Run: `docker run -p 8080:8080 huertohogar-api`

3. **Cloud Platforms**
   - AWS Elastic Beanstalk
   - Google Cloud Run
   - Heroku
   - Azure App Service

### Deployment en EC2 (Actual)

**Requisitos:**
- EC2 con Amazon Linux 2023
- Java 17+ instalado
- MySQL 8.0 instalado localmente
- Security Group: Puerto 8080 abierto
- Bucket S3 creado: `huerto-hogar-documentos`

**Comando de Inicio:**
```bash
nohup java -jar ~/huertohogar-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/huertohogar \
  --spring.datasource.username=root \
  --spring.datasource.password=${DB_PASSWORD} \
  --aws.access-key-id=${AWS_ACCESS_KEY_ID} \
  --aws.secret-access-key=TU_AWS_SECRET_KEY \
  --aws.s3.bucket-name=huerto-hogar-documentos \
  --aws.s3.region=us-east-1 \
  > ~/app.log 2>&1 &
```

**URLs en Producción:**
- Backend API: http://52.2.172.54:8080
- Swagger UI: http://52.2.172.54:8080/swagger-ui/index.html
- Frontend (S3): http://app-react-huerto-s3.s3-website-us-east-1.amazonaws.com

### Health Checks

- **Endpoint:** `/actuator/health`
- **Response:** `{ "status": "UP" }`
- **Uso:** Monitoreo de disponibilidad

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
- [ ] Paginación en listado de productos y pedidos
- [ ] Filtros avanzados de búsqueda
- [ ] Carrito de compras persistente
- [ ] Integración con pasarelas de pago (WebPay, MercadoPago)
- [ ] Notificaciones push/email al cambiar estado de pedido
- [ ] Panel de administración completo (ya implementado backend)
- [ ] Analytics y reportes de ventas
- [ ] Integración con inventario físico
- [ ] Sistema de reviews y ratings
- [ ] Programa de fidelidad/puntos
- [ ] Tracking en tiempo real de pedidos

### Mejoras Técnicas

- [ ] Redis para caching de productos
- [ ] Elasticsearch para búsqueda avanzada
- [ ] RabbitMQ/Kafka para eventos asíncronos
- [ ] Prometheus + Grafana para métricas
- [ ] Distributed tracing con Zipkin/Jaeger
- [ ] CI/CD con GitHub Actions
- [ ] Tests de carga con JMeter/Gatling
- [ ] Backup automatizado de BD y S3

## Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/)
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)

---

## Estado Actual del Proyecto

### ✅ Funcionalidades Implementadas

#### CRUD Completo
- ✅ **Productos**: Crear, Leer, Actualizar, Eliminar (Solo ADMIN para CUD)
- ✅ **Usuarios**: Crear (desde admin), Leer, Actualizar, Eliminar (Solo ADMIN)
- ✅ **Pedidos**: Crear, Leer, Actualizar estado (Solo ADMIN para actualizar)
- ✅ **Documentos**: Crear (subir a S3), Leer, Eliminar (Solo ADMIN)

#### Gestión de Pedidos
- ✅ **Estados de Pedido**: PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO/CANCELADO
- ✅ **Validación de Estados Finales**: No se puede cambiar ENTREGADO o CANCELADO
- ✅ **Datos de Entrega**: Dirección, región, comuna, comentarios, fecha

#### Integración AWS
- ✅ **S3 Storage**: Subida de archivos con validación
- ✅ **Organización de Archivos**: Por fecha (YYYY/MM/)
- ✅ **Metadatos en BD**: URLs, tamaños, tipos de archivo

#### Seguridad
- ✅ **JWT Authentication**: Tokens con expiración de 24h
- ✅ **Role-Based Access**: USER y ADMIN con permisos específicos
- ✅ **Password Encryption**: BCrypt
- ✅ **CORS**: Configurado para frontend en S3

#### Documentación
- ✅ **Swagger UI**: Documentación interactiva completa
- ✅ **OpenAPI 3.0**: Especificación exportable
- ✅ **Ejemplos**: Todos los endpoints con ejemplos

### 🚀 Deployment

- ✅ **EC2**: http://52.2.172.54:8080
- ✅ **MySQL**: Base de datos local en EC2
- ✅ **S3**: Bucket `huerto-hogar-documentos`
- ✅ **Frontend**: Desplegado en S3 Static Website

### 📊 Métricas del Proyecto

- **Endpoints REST**: 20+
- **Entidades JPA**: 5 (User, Product, Order, OrderItem, Documento)
- **DTOs**: 10+ (Request/Response separados)
- **Servicios**: 6 (Auth, User, Product, Order, Documento, S3)
- **Migraciones Flyway**: 4 versiones
- **Tests**: Unit + Integration tests

### 🎯 Listo para Presentación

- ✅ Backend funcionando en EC2
- ✅ Base de datos con datos de prueba
- ✅ S3 operativo con documentos
- ✅ CRUD completo implementado
- ✅ Panel admin preparado (endpoints listos)
- ✅ Swagger documentado
- ✅ Postman collection disponible

---

**Última actualización**: 26 de noviembre de 2024  
**Versión**: 1.0.0  
**Estado**: ✅ Producción - Listo para presentación
