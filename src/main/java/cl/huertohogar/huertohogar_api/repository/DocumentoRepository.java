package cl.huertohogar.huertohogar_api.repository;

import cl.huertohogar.huertohogar_api.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByUserEmail(String userEmail);
}
