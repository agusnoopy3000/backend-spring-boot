package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.OrderRequest;
import cl.huertohogar.huertohogar_api.dto.OrderResponse;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.Order;
import cl.huertohogar.huertohogar_api.model.OrderItem;
import cl.huertohogar.huertohogar_api.model.OrderStatus;
import cl.huertohogar.huertohogar_api.model.Product;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.OrderRepository;
import cl.huertohogar.huertohogar_api.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public OrderResponse createOrder(String email, OrderRequest request) {
        User user = userService.getUserEntityById(email);

        Order order = new Order();
        order.setUser(user);
        order.setEstado(OrderStatus.PENDIENTE);

        // CAMPOS DE ENTREGA
        order.setDireccionEntrega(
                request.getDireccionEntrega() != null && !request.getDireccionEntrega().isBlank()
                        ? request.getDireccionEntrega()
                        : user.getDireccion()
        );
        order.setRegion(request.getRegion());
        order.setComuna(request.getComuna());
        order.setComentarios(request.getComentarios());
        order.setFechaEntrega(request.getFechaEntrega());

        // Crear items y asociarlos al order
        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            Product product = productRepository.findByCodigo(itemReq.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemReq.getProductoId()));

            OrderItem item = new OrderItem();
            item.setOrder(order);  // Asociar el item al order
            item.setProductoId(product.getCodigo());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(product.getPrecio());
            return item;
        }).toList();

        order.setItems(items);

        // Calcular total
        Double total = items.stream()
                .map(i -> i.getPrecioUnitario() * i.getCantidad())
                .reduce(0.0, Double::sum);
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);
        return new OrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new OrderResponse(order);
    }

    public List<OrderResponse> getOrdersByUser(String email) {
        User user = userService.getUserEntityById(email);
        return orderRepository.findByUser(user).stream().map(OrderResponse::new).toList();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(OrderResponse::new).toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus estado) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setEstado(estado);
        return new OrderResponse(orderRepository.save(order));
    }
}
