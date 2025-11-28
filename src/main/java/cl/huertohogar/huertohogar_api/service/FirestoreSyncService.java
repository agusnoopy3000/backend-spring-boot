package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.model.Order;
import cl.huertohogar.huertohogar_api.model.OrderStatus;
import cl.huertohogar.huertohogar_api.model.User;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para sincronizar datos entre el backend (MySQL) y Firebase Firestore.
 * Permite que la app de administrador vea cambios en tiempo real.
 * 
 * IMPORTANTE: Este servicio es asíncrono y no bloquea las operaciones principales.
 * Si Firestore falla, la operación en la BD local se completa igual.
 */
@Service
@Slf4j
public class FirestoreSyncService {

    private static final String COLLECTION_PEDIDOS = "pedidos";
    private static final String COLLECTION_USERS = "users";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Verifica si Firebase está disponible
     */
    private boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Obtiene la instancia de Firestore
     */
    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    // ==================== SINCRONIZACIÓN DE PEDIDOS ====================

    /**
     * Sincroniza un pedido completo a Firestore.
     * Se ejecuta de forma asíncrona para no bloquear la operación principal.
     */
    @Async
    public CompletableFuture<Void> syncPedido(Order pedido) {
        return CompletableFuture.runAsync(() -> {
            if (!isFirebaseAvailable()) {
                log.warn("Firebase no disponible. No se sincronizará el pedido {}", pedido.getId());
                return;
            }

            try {
                Firestore db = getFirestore();
                DocumentReference docRef = db.collection(COLLECTION_PEDIDOS)
                        .document(String.valueOf(pedido.getId()));

                Map<String, Object> data = buildPedidoMap(pedido);
                docRef.set(data).get(); // .get() espera a que se complete

                log.info("✅ Pedido {} sincronizado a Firestore", pedido.getId());
            } catch (Exception e) {
                log.error("❌ Error sincronizando pedido {} a Firestore: {}", 
                        pedido.getId(), e.getMessage());
                // No lanzamos excepción para no afectar la operación principal
            }
        });
    }

    /**
     * Actualiza solo el estado de un pedido en Firestore.
     * Más eficiente que sincronizar el pedido completo.
     */
    @Async
    public CompletableFuture<Void> syncPedidoEstado(Long pedidoId, OrderStatus nuevoEstado) {
        return CompletableFuture.runAsync(() -> {
            if (!isFirebaseAvailable()) {
                log.warn("Firebase no disponible. No se actualizará estado del pedido {}", pedidoId);
                return;
            }

            try {
                Firestore db = getFirestore();
                DocumentReference docRef = db.collection(COLLECTION_PEDIDOS)
                        .document(String.valueOf(pedidoId));

                Map<String, Object> updates = new HashMap<>();
                updates.put("estado", nuevoEstado.name());
                
                docRef.update(updates).get();

                log.info("✅ Estado del pedido {} actualizado a {} en Firestore", 
                        pedidoId, nuevoEstado);
            } catch (Exception e) {
                log.error("❌ Error actualizando estado del pedido {} en Firestore: {}", 
                        pedidoId, e.getMessage());
            }
        });
    }

    /**
     * Elimina un pedido de Firestore.
     */
    @Async
    public CompletableFuture<Void> deletePedido(Long pedidoId) {
        return CompletableFuture.runAsync(() -> {
            if (!isFirebaseAvailable()) {
                log.warn("Firebase no disponible. No se eliminará pedido {} de Firestore", pedidoId);
                return;
            }

            try {
                Firestore db = getFirestore();
                db.collection(COLLECTION_PEDIDOS)
                        .document(String.valueOf(pedidoId))
                        .delete()
                        .get();

                log.info("✅ Pedido {} eliminado de Firestore", pedidoId);
            } catch (Exception e) {
                log.error("❌ Error eliminando pedido {} de Firestore: {}", 
                        pedidoId, e.getMessage());
            }
        });
    }

    // ==================== SINCRONIZACIÓN DE USUARIOS ====================

    /**
     * Sincroniza un usuario a Firestore.
     * IMPORTANTE: NO sincroniza la contraseña por seguridad.
     */
    @Async
    public CompletableFuture<Void> syncUsuario(User usuario) {
        return CompletableFuture.runAsync(() -> {
            if (!isFirebaseAvailable()) {
                log.warn("Firebase no disponible. No se sincronizará usuario {}", usuario.getEmail());
                return;
            }

            try {
                Firestore db = getFirestore();
                // Usamos el email como document ID
                DocumentReference docRef = db.collection(COLLECTION_USERS)
                        .document(usuario.getEmail());

                Map<String, Object> data = buildUsuarioMap(usuario);
                docRef.set(data).get();

                log.info("✅ Usuario {} sincronizado a Firestore", usuario.getEmail());
            } catch (Exception e) {
                log.error("❌ Error sincronizando usuario {} a Firestore: {}", 
                        usuario.getEmail(), e.getMessage());
            }
        });
    }

    /**
     * Alias de syncUsuario para mayor claridad en actualizaciones.
     */
    @Async
    public CompletableFuture<Void> updateUsuario(User usuario) {
        return syncUsuario(usuario);
    }

    /**
     * Elimina un usuario de Firestore.
     */
    @Async
    public CompletableFuture<Void> deleteUsuario(String email) {
        return CompletableFuture.runAsync(() -> {
            if (!isFirebaseAvailable()) {
                log.warn("Firebase no disponible. No se eliminará usuario {} de Firestore", email);
                return;
            }

            try {
                Firestore db = getFirestore();
                db.collection(COLLECTION_USERS)
                        .document(email)
                        .delete()
                        .get();

                log.info("✅ Usuario {} eliminado de Firestore", email);
            } catch (Exception e) {
                log.error("❌ Error eliminando usuario {} de Firestore: {}", 
                        email, e.getMessage());
            }
        });
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Construye el mapa de datos para un pedido.
     */
    private Map<String, Object> buildPedidoMap(Order pedido) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", pedido.getId());
        data.put("userEmail", pedido.getUser().getEmail());
        data.put("userName", pedido.getUser().getNombre() + " " + pedido.getUser().getApellido());
        data.put("direccionEntrega", pedido.getDireccionEntrega());
        data.put("region", pedido.getRegion());
        data.put("comuna", pedido.getComuna());
        data.put("comentarios", pedido.getComentarios());
        data.put("total", pedido.getTotal());
        data.put("estado", pedido.getEstado().name());
        
        // Fechas como String ISO
        if (pedido.getFechaEntrega() != null) {
            data.put("fechaEntrega", pedido.getFechaEntrega().format(DATE_FORMATTER));
        }
        if (pedido.getCreatedAt() != null) {
            data.put("createdAt", pedido.getCreatedAt().format(DATETIME_FORMATTER));
        }
        
        // Cantidad de items para referencia rápida
        data.put("itemsCount", pedido.getItems() != null ? pedido.getItems().size() : 0);
        
        return data;
    }

    /**
     * Construye el mapa de datos para un usuario.
     * IMPORTANTE: NO incluye la contraseña.
     */
    private Map<String, Object> buildUsuarioMap(User usuario) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("email", usuario.getEmail());
        data.put("nombre", usuario.getNombre());
        data.put("apellido", usuario.getApellido());
        data.put("run", usuario.getRun());
        data.put("direccion", usuario.getDireccion());
        data.put("telefono", usuario.getTelefono());
        data.put("rol", usuario.getRol().name());
        
        if (usuario.getCreatedAt() != null) {
            data.put("createdAt", usuario.getCreatedAt().format(DATETIME_FORMATTER));
        }
        
        // Firebase UID si existe
        if (usuario.getFirebaseUid() != null) {
            data.put("firebaseUid", usuario.getFirebaseUid());
        }
        
        return data;
    }
}
