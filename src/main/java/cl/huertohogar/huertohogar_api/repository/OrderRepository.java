package cl.huertohogar.huertohogar_api.repository;

import cl.huertohogar.huertohogar_api.model.Order;
import cl.huertohogar.huertohogar_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    @Modifying
    @Query("UPDATE Order o SET o.estado = :estado WHERE o.id = :id")
    int updateEstadoById(@Param("id") Long id, @Param("estado") Order.Estado estado);
}
