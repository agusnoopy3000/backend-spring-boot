package cl.huertohogar.huertohogar_api.repository;

import cl.huertohogar.huertohogar_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Product> findByQuery(@Param("q") String q);

    Optional<Product> findByCodigo(String codigo);
}
