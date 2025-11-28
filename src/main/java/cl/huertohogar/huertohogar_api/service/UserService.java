package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.RegisterRequest;
import cl.huertohogar.huertohogar_api.dto.UpdateProfileRequest;
import cl.huertohogar.huertohogar_api.dto.UserResponse;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.Role;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirestoreSyncService firestoreSyncService;

    public UserResponse getUserById(String email) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::new);
    }

    public User getUserEntityById(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse createUserFromAdmin(RegisterRequest request) {
        if (userRepository.existsById(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setRun(request.getRun());
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellidos());
        user.setDireccion(request.getDireccion());
        user.setTelefono(request.getTelefono());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRol(Role.USER);

        User saved = userRepository.save(user);
        
        // Sincronizar a Firestore
        firestoreSyncService.syncUsuario(saved);
        log.info("Usuario {} creado por admin y sincronizado a Firestore", saved.getEmail());
        
        return new UserResponse(saved);
    }

    public UserResponse updateUserFromAdmin(String email, RegisterRequest request) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRun(request.getRun());
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellidos());
        user.setDireccion(request.getDireccion());
        user.setTelefono(request.getTelefono());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        
        // Sincronizar a Firestore
        firestoreSyncService.updateUsuario(updated);
        log.info("Usuario {} actualizado por admin y sincronizado a Firestore", updated.getEmail());
        
        return new UserResponse(updated);
    }

    public void deleteUser(String email) {
        if (!userRepository.existsById(email)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(email);
        
        // Eliminar de Firestore
        firestoreSyncService.deleteUsuario(email);
        log.info("Usuario {} eliminado y sincronizado a Firestore", email);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     * Solo actualiza los campos que no son null en el request.
     */
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Solo actualizar campos que vienen en el request
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            user.setNombre(request.getNombre());
        }
        if (request.getApellidos() != null && !request.getApellidos().isBlank()) {
            user.setApellido(request.getApellidos());
        }
        if (request.getDireccion() != null) {
            user.setDireccion(request.getDireccion());
        }
        if (request.getTelefono() != null) {
            user.setTelefono(request.getTelefono());
        }
        if (request.getRun() != null && !request.getRun().isBlank()) {
            user.setRun(request.getRun());
        }

        User updated = userRepository.save(user);
        
        // Sincronizar a Firestore
        firestoreSyncService.updateUsuario(updated);
        log.info("Perfil de usuario {} actualizado y sincronizado a Firestore", updated.getEmail());
        
        return new UserResponse(updated);
    }

    /**
     * Elimina un usuario por su Firebase UID.
     * Usado cuando se elimina un usuario desde Firebase Console.
     * @return true si se eliminó, false si no existía
     */
    public boolean deleteUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(user -> {
                    String email = user.getEmail();
                    userRepository.delete(user);
                    firestoreSyncService.deleteUsuario(email);
                    log.info("Usuario {} eliminado por Firebase UID {} y sincronizado a Firestore", 
                            email, firebaseUid);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("No se encontró usuario con Firebase UID: {}", firebaseUid);
                    return false;
                });
    }
}
