package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.AuthResponse;
import cl.huertohogar.huertohogar_api.dto.FirebaseSyncRequest;
import cl.huertohogar.huertohogar_api.dto.UserResponse;
import cl.huertohogar.huertohogar_api.exception.BadRequestException;
import cl.huertohogar.huertohogar_api.model.Role;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Servicio para autenticación híbrida Firebase + JWT.
 * 
 * Flujo:
 * 1. El móvil autentica con Firebase y obtiene un ID Token
 * 2. El móvil envía ese token a /auth/firebase-sync
 * 3. Este servicio valida el token con Firebase Admin SDK
 * 4. Si el usuario no existe, lo crea con los datos proporcionados
 * 5. Genera un JWT del backend para usar en los demás endpoints
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Sincroniza la autenticación de Firebase con el backend.
     * 
     * @param request Contiene el token de Firebase y datos opcionales del usuario
     * @return AuthResponse con JWT del backend y datos del usuario
     * @throws BadRequestException si el token es inválido o Firebase no está configurado
     */
    @Transactional
    public AuthResponse syncWithFirebase(FirebaseSyncRequest request) {
        // Verificar que Firebase esté inicializado
        if (FirebaseApp.getApps().isEmpty()) {
            log.error("Firebase Admin SDK no está inicializado");
            throw new BadRequestException("Firebase no está configurado en el servidor. " +
                    "Contacte al administrador.");
        }

        // Verificar el token de Firebase
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance()
                    .verifyIdToken(request.getFirebaseIdToken());
        } catch (FirebaseAuthException e) {
            log.error("Error al verificar token de Firebase: {}", e.getMessage());
            throw new BadRequestException("Token de Firebase inválido o expirado: " + e.getMessage());
        }

        String email = decodedToken.getEmail();
        String firebaseUid = decodedToken.getUid();
        String displayName = decodedToken.getName();

        if (email == null || email.isBlank()) {
            throw new BadRequestException("El token de Firebase no contiene un email válido");
        }

        log.info("Token de Firebase verificado para email: {}, uid: {}", email, firebaseUid);

        // Buscar usuario existente por email o firebaseUid
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByFirebaseUid(firebaseUid))
                .orElse(null);

        if (user == null) {
            // Crear nuevo usuario
            user = createNewUser(email, firebaseUid, displayName, request);
            log.info("Nuevo usuario creado desde Firebase: {}", email);
        } else {
            // Actualizar firebaseUid si no lo tenía
            if (user.getFirebaseUid() == null) {
                user.setFirebaseUid(firebaseUid);
                user = userRepository.save(user);
                log.info("FirebaseUid actualizado para usuario existente: {}", email);
            }
            // Opcionalmente actualizar otros datos si vienen en el request
            user = updateUserDataIfProvided(user, request);
        }

        // Generar JWT del backend
        String jwtToken = generateToken(user);

        log.info("Sincronización Firebase completada para: {}", email);

        return new AuthResponse(jwtToken, new UserResponse(user));
    }

    /**
     * Genera un token JWT del backend para el usuario.
     * Usa el mismo formato que AuthService para compatibilidad.
     */
    private String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRol().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    /**
     * Crea un nuevo usuario con los datos de Firebase y el request.
     */
    private User createNewUser(String email, String firebaseUid, String displayName, 
                               FirebaseSyncRequest request) {
        User user = new User();
        user.setEmail(email);
        user.setFirebaseUid(firebaseUid);
        
        // Nombre: usar del request, o del token de Firebase, o default
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            user.setNombre(request.getNombre());
        } else if (displayName != null && !displayName.isBlank()) {
            // Intentar extraer nombre del displayName de Firebase
            String[] parts = displayName.split(" ", 2);
            user.setNombre(parts[0]);
        } else {
            user.setNombre("Usuario");
        }

        // Apellidos: usar del request o default
        if (request.getApellidos() != null && !request.getApellidos().isBlank()) {
            user.setApellido(request.getApellidos());
        } else if (displayName != null && displayName.contains(" ")) {
            // Intentar extraer apellido del displayName
            String[] parts = displayName.split(" ", 2);
            user.setApellido(parts.length > 1 ? parts[1] : "");
        } else {
            user.setApellido("");
        }

        // Otros campos opcionales
        user.setRun(request.getRun() != null ? request.getRun() : "");
        user.setDireccion(request.getDireccion() != null ? request.getDireccion() : "");
        user.setTelefono(request.getTelefono() != null ? request.getTelefono() : "");
        
        // Password: generar uno aleatorio (no se usará porque el login es por Firebase)
        user.setPassword("FIREBASE_AUTH_" + java.util.UUID.randomUUID().toString());
        
        // Rol por defecto
        user.setRol(Role.USER);

        return userRepository.save(user);
    }

    /**
     * Actualiza datos del usuario si se proporcionan en el request.
     */
    private User updateUserDataIfProvided(User user, FirebaseSyncRequest request) {
        boolean updated = false;

        if (request.getRun() != null && !request.getRun().isBlank() && 
            (user.getRun() == null || user.getRun().isBlank())) {
            user.setRun(request.getRun());
            updated = true;
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            user.setNombre(request.getNombre());
            updated = true;
        }

        if (request.getApellidos() != null && !request.getApellidos().isBlank()) {
            user.setApellido(request.getApellidos());
            updated = true;
        }

        if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
            user.setDireccion(request.getDireccion());
            updated = true;
        }

        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            user.setTelefono(request.getTelefono());
            updated = true;
        }

        if (updated) {
            user = userRepository.save(user);
            log.info("Datos de usuario actualizados: {}", user.getEmail());
        }

        return user;
    }
}
