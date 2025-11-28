package cl.huertohogar.huertohogar_api.controller;

import cl.huertohogar.huertohogar_api.dto.RegisterRequest;
import cl.huertohogar.huertohogar_api.dto.UpdateProfileRequest;
import cl.huertohogar.huertohogar_api.dto.UserResponse;
import cl.huertohogar.huertohogar_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
public class UserController {

    private final UserService userService;
    
    @Value("${firebase.webhook.secret:firebase-webhook-secret-2024}")
    private String firebaseWebhookSecret;

    @GetMapping("/me")
    @Operation(
        summary = "Obtener perfil del usuario actual",
        description = "Retorna la información del usuario autenticado",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<UserResponse> getMe(@RequestAttribute("email") String email) {
        UserResponse user = userService.getUserById(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @Operation(
        summary = "Actualizar mi perfil",
        description = "Actualiza la información del usuario autenticado. Solo se actualizan los campos enviados.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<UserResponse> updateMe(
            @RequestAttribute("email") String email,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse user = userService.updateMyProfile(email, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener todos los usuarios",
        description = "Retorna una lista paginada de todos los usuarios del sistema. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content)
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de elementos por página")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener usuario por email",
        description = "Retorna la información de un usuario específico. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content)
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Email del usuario", required = true)
            @PathVariable String email) {
        UserResponse user = userService.getUserById(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear usuario desde panel admin",
        description = "Crea un nuevo usuario con rol USER desde el panel de administración.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.createUserFromAdmin(request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza los datos de un usuario existente identificado por su email.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String email,
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.updateUserFromAdmin(email, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario identificado por su email.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    // ==================== WEBHOOK FIREBASE ====================

    /**
     * Webhook para recibir notificaciones cuando se elimina un usuario en Firebase.
     * Este endpoint debe ser llamado por una Firebase Cloud Function cuando se elimina
     * un usuario desde Firebase Console.
     * 
     * Requiere el header X-Firebase-Webhook-Secret para autenticación.
     */
    @DeleteMapping("/firebase-webhook/{firebaseUid}")
    @Operation(
        summary = "Webhook: Usuario eliminado en Firebase",
        description = "Endpoint llamado por Firebase Cloud Functions cuando se elimina un usuario. " +
                      "Requiere header X-Firebase-Webhook-Secret para autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario eliminado del backend"),
        @ApiResponse(responseCode = "204", description = "Usuario no existía en el backend"),
        @ApiResponse(responseCode = "401", description = "Secret inválido")
    })
    public ResponseEntity<Map<String, Object>> firebaseUserDeletedWebhook(
            @PathVariable String firebaseUid,
            @RequestHeader(value = "X-Firebase-Webhook-Secret", required = false) String secret) {
        
        // Validar secret
        if (secret == null || !secret.equals(firebaseWebhookSecret)) {
            log.warn("Intento de acceso al webhook Firebase con secret inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        log.info("Webhook Firebase: solicitud de eliminación para UID {}", firebaseUid);
        
        boolean deleted = userService.deleteUserByFirebaseUid(firebaseUid);
        
        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuario eliminado del backend",
                    "firebaseUid", firebaseUid
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
}
