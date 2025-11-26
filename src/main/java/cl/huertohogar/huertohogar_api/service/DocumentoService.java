package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.Documento;
import cl.huertohogar.huertohogar_api.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {
    
    private final DocumentoRepository documentoRepository;
    private final S3Service s3Service;

    /**
     * Obtiene todos los documentos
     */
    public List<Documento> findAll() {
        return documentoRepository.findAll();
    }

    /**
     * Obtiene documentos por email del usuario
     */
    public List<Documento> findByUserEmail(String userEmail) {
        return documentoRepository.findByUserEmail(userEmail);
    }

    /**
     * Busca documento por ID
     */
    public Documento findById(Long id) {
        return documentoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + id));
    }

    /**
     * Sube un archivo a S3 y guarda los metadatos en la base de datos
     */
    @Transactional
    public Documento upload(MultipartFile file, String userEmail) throws IOException {
        // Subir a S3
        String[] result = s3Service.uploadFile(file);
        String s3Key = result[0];
        String urlPublica = result[1];

        // Crear y guardar documento
        Documento documento = new Documento();
        documento.setNombre(file.getOriginalFilename());
        documento.setS3Key(s3Key);
        documento.setUrlPublica(urlPublica);
        documento.setUserEmail(userEmail);

        Documento saved = documentoRepository.save(documento);
        log.info("Documento guardado: ID={}, nombre={}, userEmail={}", 
            saved.getId(), saved.getNombre(), saved.getUserEmail());
        
        return saved;
    }

    /**
     * Elimina un documento de S3 y de la base de datos
     */
    @Transactional
    public void delete(Long id) {
        Documento documento = findById(id);
        
        // Eliminar de S3
        if (documento.getS3Key() != null && !documento.getS3Key().isEmpty()) {
            s3Service.deleteFile(documento.getS3Key());
        }
        
        // Eliminar de la base de datos
        documentoRepository.delete(documento);
        log.info("Documento eliminado: ID={}, s3Key={}", id, documento.getS3Key());
    }

    /**
     * Guarda un documento (para uso interno)
     */
    public Documento save(Documento documento) {
        return documentoRepository.save(documento);
    }
}
