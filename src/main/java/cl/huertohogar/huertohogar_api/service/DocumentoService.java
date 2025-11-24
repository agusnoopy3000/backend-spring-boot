package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.model.Documento;
import cl.huertohogar.huertohogar_api.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    private final DocumentoRepository documentoRepository;

    public Documento save(Documento documento) {
        return documentoRepository.save(documento);
    }

    public List<Documento> findByUserEmail(String userEmail) {
        return documentoRepository.findByUserEmail(userEmail);
    }

    public Documento findById(Long id) {
        return documentoRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        documentoRepository.deleteById(id);
    }
}
