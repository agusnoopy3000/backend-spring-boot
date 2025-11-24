package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request para registrar un nuevo usuario")
public class RegisterRequest {

    @NotBlank
    @Schema(description = "Nombre del usuario", example = "Juan", required = true)
    private String nombre;

    @NotBlank
    @Schema(description = "Apellido del usuario", example = "Pérez", required = true)
    private String apellido;

    @Email
    @NotBlank
    @Schema(description = "Email del usuario (será usado como identificador único)", example = "juan.perez@example.com", required = true)
    private String email;

    @NotBlank
    @Size(min = 6)
    @Schema(description = "Contraseña del usuario (mínimo 6 caracteres)", example = "MyS3cur3P@ssw0rd!", required = true, minLength = 6)
    private String password;

    @NotBlank
    @Schema(description = "Dirección del usuario", example = "Av. Principal 123, Santiago", required = true)
    private String direccion;

    @NotBlank
    @Schema(description = "Teléfono de contacto", example = "+56912345678", required = true)
    private String telefono;
}
