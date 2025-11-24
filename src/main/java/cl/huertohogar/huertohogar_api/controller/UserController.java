package cl.huertohogar.huertohogar_api.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
public class UserController {

    private final UserService userService;

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
}
