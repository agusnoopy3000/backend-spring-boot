# Huertohogar API

Backend API para la aplicación Huertohogar, construida con Spring Boot 3.x.

## Tecnologías

- Java 21 LTS
- Spring Boot 3.x
- JPA (Hibernate)
- MySQL (producción) / H2 (desarrollo)
- JWT para autenticación
- Swagger/OpenAPI para documentación
- Flyway para migraciones
- Docker para contenedorización

## Requisitos

- JDK 21
- Maven 3.6+
- MySQL (para producción)

## Configuración

### Variables de Entorno

- `JDBC_URL`: URL de la base de datos MySQL (ej: jdbc:mysql://localhost:3306/huertohogar)
- `DB_USERNAME`: Usuario de la base de datos
- `DB_PASSWORD`: Contraseña de la base de datos
- `JWT_SECRET`: Secreto para JWT (ej: mySecretKey)

### Perfiles

- `dev`: Usa H2 en memoria
- `prod`: Usa MySQL

## Ejecución

### Desarrollo

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Producción

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Con Docker

```bash
docker build -t huertohogar-api .
docker run -p 8080:8080 -e JDBC_URL=jdbc:mysql://host:3306/db -e DB_USERNAME=user -e DB_PASSWORD=pass -e JWT_SECRET=secret huertohogar-api
```

## Endpoints Principales

### Autenticación
- `POST /auth/register` - Registrar usuario
- `POST /auth/login` - Iniciar sesión

### Usuarios
- `GET /users/me` - Perfil del usuario actual
- `GET /users` - Lista de usuarios (Admin)

### Productos
- `GET /products` - Lista de productos
- `GET /products/{id}` - Detalle de producto
- `POST /products` - Crear producto (Admin)
- `PUT /products/{id}` - Actualizar producto (Admin)
- `DELETE /products/{id}` - Eliminar producto (Admin)

### Pedidos
- `POST /orders` - Crear pedido
- `GET /orders/{id}` - Detalle de pedido
- `GET /orders` - Pedidos del usuario

### Documentación
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Usuarios de Prueba

- Admin: admin@huertohogar.cl / min123
- Cliente: cliente@demo.com / password

## Pruebas

```bash
mvn test
```

## Despliegue

### Cloud Run

1. Construir imagen Docker
2. Subir a Google Container Registry
3. Desplegar en Cloud Run con variables de entorno

Ejemplo:

```bash
gcloud run deploy huertohogar-api \
  --image gcr.io/PROJECT-ID/huertohogar-api \
  --platform managed \
  --region us-central1 \
  --set-env-vars JDBC_URL=jdbc:mysql://... \
  --allow-unauthenticated
```

## Estructura del Proyecto

```
src/main/java/cl/huertohogar/huertohogar_api/
├── controller/     # Controladores REST
├── dto/            # Objetos de transferencia de datos
├── exception/      # Manejo de excepciones
├── model/          # Entidades JPA
├── repository/     # Repositorios
├── service/        # Lógica de negocio
└── config/         # Configuraciones
```

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request
