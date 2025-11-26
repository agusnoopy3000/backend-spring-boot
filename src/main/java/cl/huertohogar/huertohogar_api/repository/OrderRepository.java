package cl.huertohogar.huertohogar_api.repository;

import cl.huertohogar.huertohogar_api.model.Order;
import cl.huertohogar.huertohogar_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
