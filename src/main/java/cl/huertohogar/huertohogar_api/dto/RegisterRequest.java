package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request para registrar un nuevo usuario")
public class RegisterRequest {

    // NUEVO: RUN/RUT del usuario (obligatorio para web)
    @NotBlank(message = "El RUN es obligatorio")
    @Size(max = 20, message = "El RUN no puede superar 20 caracteres")
    @Schema(description = "RUN/RUT del usuario", example = "19.011.022-K")
    private String run;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    // RENOMBRADO LÓGICAMENTE: apellidos en vez de apellido para ajustar al front
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 50, message = "Los apellidos deben tener entre 2 y 50 caracteres")
    @Schema(description = "Apellidos del usuario", example = "Pérez González")
    private String apellidos;

    @Email(message = "El email no tiene un formato válido")
    @NotBlank(message = "El email es obligatorio")
    @Size(max = 100, message = "El email no puede superar 100 caracteres")
    @Schema(description = "Email del usuario (será usado como identificador único)", example = "juan.perez@example.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 7, max = 100, message = "La contraseña debe tener al menos 7 caracteres")
    // Si quisieras exigir caracter especial, podrías activar este patrón:
    // @Pattern(
    //   regexp = "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'`<>,.?/\\\\]).{7,100}$",
    //   message = "La contraseña debe incluir al menos un carácter especial"
    // )
    @Schema(description = "Contraseña del usuario (mínimo 7 caracteres)", example = "MyS3cur3P@ssw0rd!", minLength = 7)
    private String password;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    @Schema(description = "Dirección del usuario", example = "Av. Principal 123, Santiago")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "El teléfono debe ser un número válido")
    @Schema(description = "Teléfono de contacto", example = "+56912345678")
    private String telefono;
}
