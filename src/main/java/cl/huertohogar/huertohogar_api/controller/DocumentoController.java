package cl.huertohogar.huertohogar_api.controller;

import cl.huertohogar.huertohogar_api.model.Documento;
import cl.huertohogar.huertohogar_api.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documentos", description = "Endpoints para gestión de documentos en S3")
public class DocumentoController {
    
    private final DocumentoService documentoService;

    @GetMapping
    @Operation(
        summary = "Listar todos los documentos",
        description = "Retorna todos los documentos almacenados. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de documentos obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content)
    })
    public ResponseEntity<List<Documento>> getAllDocumentos(
            @RequestAttribute(value = "role", required = false) String role) {
        // Verificar que sea ADMIN
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        List<Documento> documentos = documentoService.findAll();
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/mis-documentos")
    @Operation(
        summary = "Obtener mis documentos",
        description = "Retorna los documentos del usuario autenticado.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de documentos del usuario"),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<List<Documento>> getMisDocumentos(
            @RequestAttribute("email") String email) {
        List<Documento> documentos = documentoService.findByUserEmail(email);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener documento por ID",
        description = "Retorna los detalles de un documento específico. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documento encontrado",
            content = @Content(schema = @Schema(implementation = Documento.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    public ResponseEntity<Documento> getById(
            @Parameter(description = "ID del documento", required = true)
            @PathVariable Long id,
            @RequestAttribute(value = "role", required = false) String role) {
        // Verificar que sea ADMIN
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        Documento documento = documentoService.findById(id);
        return ResponseEntity.ok(documento);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Subir documento",
        description = "Sube un documento a S3 y guarda sus metadatos. Solo accesible para administradores. " +
                     "Tipos permitidos: PDF, DOC, DOCX, XLS, XLSX, JPG, PNG, GIF, TXT, CSV. Tamaño máximo: 10MB.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documento subido exitosamente",
            content = @Content(schema = @Schema(implementation = Documento.class))),
        @ApiResponse(responseCode = "400", description = "Archivo vacío o tipo no permitido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "413", description = "Archivo excede tamaño máximo (10MB)", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error al subir a S3", content = @Content)
    })
    public ResponseEntity<?> upload(
            @Parameter(description = "Archivo a subir", required = true)
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("email") String email,
            @RequestAttribute(value = "role", required = false) String role) {
        
        log.info("Solicitud de subida de archivo recibida de usuario: {} con rol: {}", email, role);
        
        // Verificar que sea ADMIN
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            log.warn("Acceso denegado a {}: no tiene rol ADMIN", email);
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol ADMIN");
        }

        try {
            log.info("Subiendo archivo: {} ({}  bytes)", file.getOriginalFilename(), file.getSize());
            Documento documento = documentoService.upload(file, email);
            log.info("Documento subido exitosamente por {}: {}", email, documento.getNombre());
            return ResponseEntity.ok(documento);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al subir documento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            log.error("Error de I/O al subir documento: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al procesar el archivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al subir documento: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al subir documento: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar documento",
        description = "Elimina un documento de S3 y de la base de datos. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Documento eliminado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error al eliminar de S3", content = @Content)
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del documento a eliminar", required = true)
            @PathVariable Long id,
            @RequestAttribute("email") String email,
            @RequestAttribute(value = "role", required = false) String role) {
        
        // Verificar que sea ADMIN
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol ADMIN");
        }

        try {
            documentoService.delete(id);
            log.info("Documento {} eliminado por {}", id, email);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar documento {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al eliminar documento: " + e.getMessage());
        }
    }
}
