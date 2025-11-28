package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request para actualizar el perfil del usuario")
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Size(min = 2, max = 50, message = "Los apellidos deben tener entre 2 y 50 caracteres")
    @Schema(description = "Apellidos del usuario", example = "Pérez González")
    private String apellidos;

    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    @Schema(description = "Dirección del usuario", example = "Av. Principal 123, Santiago")
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Pattern(regexp = "^(\\+?[0-9]{9,15})?$", message = "El teléfono debe ser un número válido")
    @Schema(description = "Teléfono de contacto", example = "+56912345678")
    private String telefono;

    @Size(max = 20, message = "El RUN no puede superar 20 caracteres")
    @Schema(description = "RUN/RUT del usuario", example = "19.011.022-K")
    private String run;
}
