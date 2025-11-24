package cl.huertohogar.huertohogar_api.controller;

import cl.huertohogar.huertohogar_api.model.Documento;
import cl.huertohogar.huertohogar_api.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {
    private final DocumentoService documentoService;

    @GetMapping("/mis-documentos")
    public List<Documento> getMisDocumentos(@RequestParam String userEmail) {
        return documentoService.findByUserEmail(userEmail);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documento> getById(@PathVariable Long id) {
        Documento doc = documentoService.findById(id);
        return doc != null ? ResponseEntity.ok(doc) : ResponseEntity.notFound().build();
    }

    @PostMapping("/upload")
    public Documento upload(@RequestBody Documento documento) {
        // Aquí deberías integrar la subida a S3, por ahora solo guarda metadatos
        return documentoService.save(documento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
