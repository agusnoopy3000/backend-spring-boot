package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para sincronización de autenticación Firebase con el backend.
 * El móvil envía el token de Firebase y opcionalmente datos adicionales del usuario.
 */
@Data
@Schema(description = "Request para sincronizar autenticación Firebase con el backend")
public class FirebaseSyncRequest {

    @NotBlank(message = "El token de Firebase es obligatorio")
    @Schema(description = "ID Token de Firebase obtenido del cliente móvil", 
            example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", 
            required = true)
    private String firebaseIdToken;

    @Size(max = 20, message = "El RUN no puede superar 20 caracteres")
    @Schema(description = "RUN/RUT del usuario (opcional, para registro inicial)", 
            example = "19.011.022-K")
    private String run;

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario (opcional, para registro inicial)", 
            example = "Juan")
    private String nombre;

    @Size(min = 2, max = 50, message = "Los apellidos deben tener entre 2 y 50 caracteres")
    @Schema(description = "Apellidos del usuario (PLURAL, opcional)", 
            example = "Pérez González")
    private String apellidos;

    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    @Schema(description = "Dirección del usuario (opcional)", 
            example = "Av. Principal 123, Santiago")
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Schema(description = "Teléfono del usuario (opcional)", 
            example = "+56912345678")
    private String telefono;
}
