package cl.huertohogar.huertohogar_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "documentos")
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;
    @Column(name = "s3_key")
    private String s3Key;
    @Column(name = "url_publica")
    private String urlPublica;
    @Column(name = "user_email")
    private String userEmail;
}
