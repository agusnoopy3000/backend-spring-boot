package cl.huertohogar.huertohogar_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documentos")
@Schema(description = "Entidad que representa un documento almacenado en S3")
public class Documento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del documento", example = "1")
    private Long id;

    @Column(name = "nombre", nullable = false)
    @Schema(description = "Nombre original del archivo", example = "factura-noviembre.pdf")
    private String nombre;
    
    @Column(name = "s3_key", nullable = false, length = 500)
    @Schema(description = "Key del archivo en S3", example = "documentos/2025/11/uuid-factura.pdf")
    private String s3Key;
    
    @Column(name = "url_publica", length = 1000)
    @Schema(description = "URL pública para acceder al archivo", example = "https://bucket.s3.amazonaws.com/...")
    private String urlPublica;
    
    @Column(name = "user_email", nullable = false)
    @Schema(description = "Email del usuario que subió el documento", example = "admin@huertohogar.cl")
    private String userEmail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Fecha y hora de creación")
    private LocalDateTime createdAt;
}
