# Huertohogar API

Backend API para la aplicación Huertohogar, construida con Spring Boot 3.x.

## Tecnologías

- Java 17 LTS
- Spring Boot 3.x
- JPA (Hibernate)
- MySQL (producción) / H2 (desarrollo)
- JWT para autenticación
- Swagger/OpenAPI para documentación
- Flyway para migraciones
- Docker para contenedorización

## Requisitos

- JDK 17
- Maven 3.6+
- MySQL (para producción)
- Docker (opcional)

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

### Amazon EC2 (Backend Principal)

#### Opción 1: Despliegue con Docker

1. **Preparar instancia EC2**:
   - Lanzar instancia EC2 (recomendado: t2.micro o t2.small)
   - Sistema operativo: Amazon Linux 2 o Ubuntu 20.04+
   - Abrir puertos en Security Group: 22 (SSH), 80 (HTTP), 8080 (App)

2. **Instalar Docker en EC2**:
   ```bash
   # Para Amazon Linux 2
   sudo yum update -y
   sudo yum install docker -y
   sudo service docker start
   sudo usermod -a -G docker ec2-user
   
   # Para Ubuntu
   sudo apt-get update
   sudo apt-get install docker.io -y
   sudo systemctl start docker
   sudo systemctl enable docker
   ```

3. **Construir y ejecutar la aplicación**:
   ```bash
   # Clonar el repositorio
   git clone https://github.com/agusnoopy3000/backend-spring-boot.git
   cd backend-spring-boot
   
   # Construir imagen Docker
   docker build -t huertohogar-api .
   
   # Ejecutar contenedor (ajustar variables de entorno según configuración)
   docker run -d -p 8080:8080 \
     -e JDBC_URL=jdbc:mysql://your-rds-endpoint:3306/huertohogar \
     -e DB_USERNAME=admin \
     -e DB_PASSWORD=your-password \
     -e JWT_SECRET=your-super-secret-key-min-256-bits \
     --name huertohogar-api \
     huertohogar-api
   ```

#### Opción 2: Despliegue Directo (sin Docker)

1. **Preparar instancia EC2**:
   - Lanzar instancia EC2 con al menos 1 GB RAM
   - Instalar Java 17:
   ```bash
   # Para Amazon Linux 2
   sudo yum install java-17-amazon-corretto -y
   
   # Para Ubuntu
   sudo apt-get update
   sudo apt-get install openjdk-17-jdk -y
   ```

2. **Instalar Maven**:
   ```bash
   # Para Amazon Linux 2
   sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
   sudo tar xzf apache-maven-3.9.6-bin.tar.gz -C /opt
   sudo ln -s /opt/apache-maven-3.9.6 /opt/maven
   export PATH=/opt/maven/bin:$PATH
   
   # Para Ubuntu
   sudo apt-get install maven -y
   ```

3. **Compilar y ejecutar la aplicación**:
   ```bash
   # Clonar repositorio
   git clone https://github.com/agusnoopy3000/backend-spring-boot.git
   cd backend-spring-boot
   
   # Compilar
   mvn clean package -DskipTests
   
   # Configurar variables de entorno
   export JDBC_URL=jdbc:mysql://your-rds-endpoint:3306/huertohogar
   export DB_USERNAME=admin
   export DB_PASSWORD=your-password
   export JWT_SECRET=your-super-secret-key-min-256-bits
   
   # Ejecutar aplicación
   nohup java -jar target/huertohogar-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
   ```

4. **Configurar como servicio systemd** (opcional pero recomendado):
   ```bash
   sudo nano /etc/systemd/system/huertohogar-api.service
   ```
   
   Contenido del archivo:
   ```ini
   [Unit]
   Description=Huertohogar API Service
   After=network.target

   [Service]
   Type=simple
   User=ec2-user
   WorkingDirectory=/home/ec2-user/backend-spring-boot
   ExecStart=/usr/bin/java -jar /home/ec2-user/backend-spring-boot/target/huertohogar-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   Environment="JDBC_URL=jdbc:mysql://your-rds-endpoint:3306/huertohogar"
   Environment="DB_USERNAME=admin"
   Environment="DB_PASSWORD=your-password"
   Environment="JWT_SECRET=your-super-secret-key-min-256-bits"
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```
   
   Activar servicio:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable huertohogar-api
   sudo systemctl start huertohogar-api
   sudo systemctl status huertohogar-api
   ```

### Base de Datos - Amazon RDS (Recomendado)

1. **Crear instancia MySQL en RDS**:
   - Motor: MySQL 8.0
   - Plantilla: Free tier o Producción según necesidades
   - Identificador: huertohogar-db
   - Usuario maestro: admin
   - Contraseña: [elegir contraseña segura]
   - Habilitar acceso público si es necesario para desarrollo

2. **Configurar Security Group**:
   - Permitir conexiones en puerto 3306 desde el Security Group de EC2

3. **Crear base de datos**:
   ```sql
   CREATE DATABASE huertohogar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

### Amazon S3 (Almacenamiento de Archivos Estáticos)

Si necesitas almacenar archivos estáticos como imágenes de productos:

1. **Crear bucket S3**:
   ```bash
   aws s3 mb s3://huertohogar-assets
   ```

2. **Configurar política de bucket para acceso público** (solo para archivos públicos):
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "PublicReadGetObject",
         "Effect": "Allow",
         "Principal": "*",
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::huertohogar-assets/*"
       }
     ]
   }
   ```

3. **Configurar CORS en S3**:
   ```json
   [
     {
       "AllowedHeaders": ["*"],
       "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
       "AllowedOrigins": ["*"],
       "ExposeHeaders": []
     }
   ]
   ```

4. **Agregar dependencias de AWS S3 al proyecto** (si aún no están):
   ```xml
   <dependency>
     <groupId>com.amazonaws</groupId>
     <artifactId>aws-java-sdk-s3</artifactId>
     <version>1.12.529</version>
   </dependency>
   ```

### Configuración de Red y Seguridad

1. **Configurar Load Balancer** (opcional pero recomendado para producción):
   - Crear Application Load Balancer
   - Configurar Target Group apuntando a EC2:8080
   - Configurar SSL/TLS con certificado de AWS Certificate Manager

2. **Configurar Route 53** (DNS):
   - Crear zona hospedada para tu dominio
   - Crear registro A apuntando al Load Balancer o IP elástica de EC2

3. **Variables de Entorno para Producción**:
   ```bash
   JDBC_URL=jdbc:mysql://huertohogar-db.xxxxx.us-east-1.rds.amazonaws.com:3306/huertohogar
   DB_USERNAME=admin
   DB_PASSWORD=[contraseña-segura]
   JWT_SECRET=[clave-de-al-menos-256-bits]
   ```

### Monitoreo y Logs

1. **CloudWatch Logs**:
   - Instalar CloudWatch agent en EC2
   - Configurar para enviar logs de aplicación

2. **Health Checks**:
   - Endpoint de health: `http://your-domain:8080/actuator/health`
   - Configurar en Load Balancer o monitoreo externo

### Cloud Run (Alternativa)

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
