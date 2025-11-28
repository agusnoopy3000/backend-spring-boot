package cl.huertohogar.huertohogar_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Column(name = "run", length = 20)
    private String run;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Role rol;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
