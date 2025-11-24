package cl.huertohogar.huertohogar_api.dto;

import cl.huertohogar.huertohogar_api.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private String email;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String rol;
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.nombre = user.getNombre();
        this.apellido = user.getApellido();
        this.direccion = user.getDireccion();
        this.telefono = user.getTelefono();
        this.rol = user.getRol();
        this.createdAt = user.getCreatedAt();
    }
}
