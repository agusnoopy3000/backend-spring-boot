package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para iniciar sesión")
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "Email del usuario registrado", example = "cliente@demo.com", required = true)
    private String email;

    @NotBlank
    @Schema(description = "Contraseña del usuario", example = "password", required = true)
    private String password;
}
