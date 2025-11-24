# Guía de Uso de la API - Huerto Hogar

Esta guía proporciona ejemplos prácticos de cómo utilizar la API de Huerto Hogar desde clientes móviles (Kotlin) y web (React/JavaScript).

## Tabla de Contenidos

1. [Autenticación](#autenticación)
2. [Gestión de Productos](#gestión-de-productos)
3. [Gestión de Usuarios](#gestión-de-usuarios)
4. [Gestión de Pedidos](#gestión-de-pedidos)
5. [Ejemplos para Kotlin](#ejemplos-para-kotlin)
6. [Ejemplos para React/JavaScript](#ejemplos-para-reactjavascript)

## Base URL

```
Desarrollo: http://localhost:8080
Producción: https://api.huertohogar.cl
```

**Importante:** Siempre use HTTPS en producción para proteger los tokens JWT y datos sensibles.

## Autenticación

### 1. Registrar Usuario

**Endpoint:** `POST /auth/register`

**Request:**
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@example.com",
  "password": "password123",
  "direccion": "Av. Principal 123, Santiago",
  "telefono": "+56912345678"
}
```

**Response:** (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "email": "juan.perez@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "direccion": "Av. Principal 123, Santiago",
    "telefono": "+56912345678",
    "rol": "user"
  }
}
```

### 2. Iniciar Sesión

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "email": "juan.perez@example.com",
  "password": "password123"
}
```

**Response:** (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "email": "juan.perez@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "direccion": "Av. Principal 123, Santiago",
    "telefono": "+56912345678",
    "rol": "user"
  }
}
```

**Uso del Token:**

Para todos los endpoints protegidos, incluir el token en el header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Gestión de Productos

### 1. Listar Todos los Productos

**Endpoint:** `GET /products`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters (opcionales):**
- `q`: término de búsqueda

**Response:** (200 OK)
```json
[
  {
    "id": 1,
    "codigo": "VRD-001",
    "nombre": "Tomate Cherry",
    "descripcion": "Tomates cherry frescos de cultivo orgánico",
    "precio": 2500.0,
    "stock": 100,
    "imagen": "https://example.com/images/tomate.jpg",
    "categoria": "Verduras"
  },
  {
    "id": 2,
    "codigo": "VRD-002",
    "nombre": "Lechuga Hidropónica",
    "descripcion": "Lechuga fresca de invernadero",
    "precio": 1800.0,
    "stock": 50,
    "imagen": "https://example.com/images/lechuga.jpg",
    "categoria": "Verduras"
  }
]
```

### 2. Buscar Productos

**Endpoint:** `GET /products?q=tomate`

**Response:** Filtra productos por nombre o descripción

### 3. Obtener Producto por ID

**Endpoint:** `GET /products/{id}`

**Response:** (200 OK)
```json
{
  "id": 1,
  "codigo": "VRD-001",
  "nombre": "Tomate Cherry",
  "descripcion": "Tomates cherry frescos de cultivo orgánico",
  "precio": 2500.0,
  "stock": 100,
  "imagen": "https://example.com/images/tomate.jpg",
  "categoria": "Verduras"
}
```

### 4. Crear Producto (Admin)

**Endpoint:** `POST /products`

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Request:**
```json
{
  "codigo": "VRD-003",
  "nombre": "Zanahoria Orgánica",
  "descripcion": "Zanahorias frescas cultivadas orgánicamente",
  "precio": 1500.0,
  "stock": 200,
  "imagen": "https://example.com/images/zanahoria.jpg",
  "categoria": "Verduras"
}
```

**Response:** (200 OK)
```json
{
  "id": 3,
  "codigo": "VRD-003",
  "nombre": "Zanahoria Orgánica",
  "descripcion": "Zanahorias frescas cultivadas orgánicamente",
  "precio": 1500.0,
  "stock": 200,
  "imagen": "https://example.com/images/zanahoria.jpg",
  "categoria": "Verduras"
}
```

### 5. Actualizar Producto (Admin)

**Endpoint:** `PUT /products/{id}`

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Request:**
```json
{
  "codigo": "VRD-003",
  "nombre": "Zanahoria Orgánica Premium",
  "descripcion": "Zanahorias premium cultivadas orgánicamente",
  "precio": 1800.0,
  "stock": 180,
  "imagen": "https://example.com/images/zanahoria-premium.jpg",
  "categoria": "Verduras"
}
```

### 6. Eliminar Producto (Admin)

**Endpoint:** `DELETE /products/{id}`

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Response:** (204 No Content)

## Gestión de Usuarios

### 1. Obtener Perfil Actual

**Endpoint:** `GET /users/me`

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** (200 OK)
```json
{
  "email": "juan.perez@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "direccion": "Av. Principal 123, Santiago",
  "telefono": "+56912345678",
  "rol": "user"
}
```

### 2. Listar Todos los Usuarios (Admin)

**Endpoint:** `GET /users?page=0&size=10`

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Response:** (200 OK)
```json
{
  "content": [
    {
      "email": "juan.perez@example.com",
      "nombre": "Juan",
      "apellido": "Pérez",
      "direccion": "Av. Principal 123, Santiago",
      "telefono": "+56912345678",
      "rol": "user"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 3. Obtener Usuario por Email (Admin)

**Endpoint:** `GET /users/{email}`

**Headers:**
```
Authorization: Bearer {admin_token}
```

## Gestión de Pedidos

### 1. Crear Pedido

**Endpoint:** `POST /orders`

**Headers:**
```
Authorization: Bearer {token}
```

**Request:**
```json
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
```

**Response:** (200 OK)
```json
{
  "id": 1,
  "user": {
    "email": "juan.perez@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "direccion": "Av. Principal 123, Santiago",
    "telefono": "+56912345678",
    "rol": "user"
  },
  "items": [
    {
      "id": 1,
      "productoId": "VRD-001",
      "cantidad": 5,
      "precioUnitario": 2500.0
    },
    {
      "id": 2,
      "productoId": "VRD-002",
      "cantidad": 3,
      "precioUnitario": 1800.0
    }
  ],
  "total": 17900.0,
  "estado": "PENDIENTE",
  "createdAt": "2025-11-24T10:30:00"
}
```

### 2. Obtener Pedido por ID

**Endpoint:** `GET /orders/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** Similar al response de crear pedido

### 3. Listar Pedidos del Usuario

**Endpoint:** `GET /orders`

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** (200 OK)
```json
[
  {
    "id": 1,
    "user": {...},
    "items": [...],
    "total": 17900.0,
    "estado": "PENDIENTE",
    "createdAt": "2025-11-24T10:30:00"
  }
]
```

### 4. Listar Pedidos de Otro Usuario (Admin)

**Endpoint:** `GET /orders?userEmail=juan.perez@example.com`

**Headers:**
```
Authorization: Bearer {admin_token}
```

### 5. Actualizar Estado del Pedido (Admin)

**Endpoint:** `PUT /orders/{id}/status`

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Request:**
```json
{
  "estado": "CONFIRMADO"
}
```

**Valores posibles para estado:**
- `PENDIENTE`
- `CONFIRMADO`
- `ENVIADO`
- `ENTREGADO`
- `CANCELADO`

## Ejemplos para Kotlin

### Configuración de Retrofit

```kotlin
// ApiService.kt
interface ApiService {
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("products")
    suspend fun getProducts(@Query("q") searchQuery: String?): List<ProductResponse>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): ProductResponse
    
    @POST("products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Body product: ProductRequest
    ): ProductResponse
    
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body order: OrderRequest
    ): OrderResponse
    
    @GET("orders")
    suspend fun getMyOrders(
        @Header("Authorization") token: String
    ): List<OrderResponse>
    
    @GET("users/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): UserResponse
}

// Retrofit Configuration
object RetrofitClient {
    // Para desarrollo local (emulador Android)
    private const val BASE_URL_DEV = "http://10.0.2.2:8080/"
    // Para producción (usar HTTPS)
    private const val BASE_URL_PROD = "https://api.huertohogar.cl/"
    // Para dispositivo físico en desarrollo usar IP de la computadora: "http://192.168.1.X:8080/"
    
    private val BASE_URL = BASE_URL_DEV // Cambiar a BASE_URL_PROD en producción
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

### Modelos de Datos (Kotlin)

```kotlin
// Data Classes
data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val direccion: String,
    val telefono: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val email: String,
    val nombre: String,
    val apellido: String,
    val direccion: String,
    val telefono: String,
    val rol: String
)

data class ProductResponse(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagen: String,
    val categoria: String
)

data class OrderRequest(
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val productoId: String,
    val cantidad: Int,
    val precioUnitario: Double
)

data class OrderResponse(
    val id: Long,
    val user: UserResponse,
    val items: List<OrderItemResponse>,
    val total: Double,
    val estado: String,
    val createdAt: String
)
```

### Uso en ViewModel

```kotlin
class AuthViewModel : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val response = RetrofitClient.api.login(LoginRequest(email, password))
                // Guardar token en SharedPreferences
                saveToken(response.token)
                _authState.value = AuthState.Success(response)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val response = RetrofitClient.api.register(request)
                saveToken(response.token)
                _authState.value = AuthState.Success(response)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

class ProductViewModel : ViewModel() {
    
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products
    
    fun loadProducts(searchQuery: String? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getProducts(searchQuery)
                _products.value = response
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}

class OrderViewModel : ViewModel() {
    
    fun createOrder(token: String, items: List<OrderItemRequest>) {
        viewModelScope.launch {
            try {
                val request = OrderRequest(items)
                val response = RetrofitClient.api.createOrder("Bearer $token", request)
                // Manejar respuesta exitosa
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
```

## Ejemplos para React/JavaScript

### Configuración de Axios

```javascript
// api/axios.js
import axios from 'axios';

const BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
// Para producción, configurar REACT_APP_API_URL=https://api.huertohogar.cl

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
```

### Servicios API

```javascript
// services/authService.js
import api from '../api/axios';

export const authService = {
  async register(userData) {
    const response = await api.post('/auth/register', userData);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response.data;
  },

  async login(email, password) {
    const response = await api.post('/auth/login', { email, password });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
};

// services/productService.js
import api from '../api/axios';

export const productService = {
  async getAll(searchQuery = '') {
    const response = await api.get('/products', {
      params: { q: searchQuery }
    });
    return response.data;
  },

  async getById(id) {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },

  async create(productData) {
    const response = await api.post('/products', productData);
    return response.data;
  },

  async update(id, productData) {
    const response = await api.put(`/products/${id}`, productData);
    return response.data;
  },

  async delete(id) {
    await api.delete(`/products/${id}`);
  }
};

// services/orderService.js
import api from '../api/axios';

export const orderService = {
  async create(orderData) {
    const response = await api.post('/orders', orderData);
    return response.data;
  },

  async getMyOrders() {
    const response = await api.get('/orders');
    return response.data;
  },

  async getById(id) {
    const response = await api.get(`/orders/${id}`);
    return response.data;
  },

  async updateStatus(id, estado) {
    const response = await api.put(`/orders/${id}/status`, { estado });
    return response.data;
  }
};
```

### Uso en Componentes React

```javascript
// components/Login.jsx
import React, { useState } from 'react';
import { authService } from '../services/authService';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await authService.login(email, password);
      window.location.href = '/products'; // o usar React Router
    } catch (err) {
      setError('Credenciales inválidas');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Contraseña"
        required
      />
      {error && <p className="error">{error}</p>}
      <button type="submit">Iniciar Sesión</button>
    </form>
  );
}

// components/ProductList.jsx
import React, { useState, useEffect } from 'react';
import { productService } from '../services/productService';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadProducts();
  }, [searchQuery]);

  const loadProducts = async () => {
    try {
      const data = await productService.getAll(searchQuery);
      setProducts(data);
    } catch (error) {
      console.error('Error al cargar productos:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Cargando...</div>;

  return (
    <div>
      <input
        type="text"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        placeholder="Buscar productos..."
      />
      <div className="product-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <img src={product.imagen} alt={product.nombre} />
            <h3>{product.nombre}</h3>
            <p>{product.descripcion}</p>
            <p className="price">${product.precio}</p>
            <p>Stock: {product.stock}</p>
            <button>Agregar al carrito</button>
          </div>
        ))}
      </div>
    </div>
  );
}

// components/CreateOrder.jsx
import React, { useState } from 'react';
import { orderService } from '../services/orderService';

function CreateOrder({ cartItems }) {
  const [loading, setLoading] = useState(false);

  const handleCreateOrder = async () => {
    setLoading(true);
    try {
      const orderData = {
        items: cartItems.map(item => ({
          productoId: item.codigo,
          cantidad: item.quantity,
          precioUnitario: item.precio
        }))
      };
      
      const order = await orderService.create(orderData);
      alert(`Pedido creado exitosamente. ID: ${order.id}`);
      // Limpiar carrito, redirigir, etc.
    } catch (error) {
      alert('Error al crear el pedido');
    } finally {
      setLoading(false);
    }
  };

  return (
    <button onClick={handleCreateOrder} disabled={loading}>
      {loading ? 'Procesando...' : 'Confirmar Pedido'}
    </button>
  );
}
```

## Manejo de Errores

### Respuestas de Error Comunes

**400 Bad Request:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/products"
}
```

**401 Unauthorized:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado",
  "path": "/orders"
}
```

**403 Forbidden:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Acceso denegado",
  "path": "/products"
}
```

**404 Not Found:**
```json
{
  "timestamp": "2025-11-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found",
  "path": "/products/999"
}
```

## Testing con Postman

Se incluye una colección de Postman en el archivo `Huertohogar_API.postman_collection.json` con ejemplos de todos los endpoints.

### Configurar Variables en Postman

1. Crear entorno "Development"
2. Agregar variables:
   - `baseUrl`: `http://localhost:8080`
   - `token`: (se actualiza automáticamente después del login)

### Scripts de Postman

Agregar este script en la pestaña "Tests" del endpoint de login:

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
}
```

Esto guardará automáticamente el token para usarlo en otros requests.

## Notas de Seguridad

1. **HTTPS en Producción:** Siempre usar HTTPS en producción para proteger tokens
2. **Token Storage:** En web usar localStorage/sessionStorage, en móvil usar encriptación
3. **Token Expiration:** Los tokens expiran en 24 horas, implementar refresh automático
4. **CORS:** Configurar correctamente los orígenes permitidos
5. **Validation:** Validar siempre datos en cliente Y servidor
6. **Rate Limiting:** Implementar límite de requests si es necesario

## Soporte

Para problemas o preguntas:
- Email: info@huertohogar.cl
- Documentación Swagger: http://localhost:8080/swagger-ui/index.html
