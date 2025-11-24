package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.OrderRequest;
import cl.huertohogar.huertohogar_api.dto.OrderResponse;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.Order;
import cl.huertohogar.huertohogar_api.model.OrderItem;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;

    @Transactional
    public OrderResponse createOrder(String email, OrderRequest request) {
        User user = userService.getUserEntityById(email);

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            OrderItem item = new OrderItem();
            item.setProductoId(itemReq.getProductoId());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(itemReq.getPrecioUnitario());
            return item;
        }).toList();

        Double total = items.stream()
                .map(i -> i.getPrecioUnitario() * i.getCantidad())
                .reduce(0.0, Double::sum);

        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotal(total);
        order.setEstado(Order.Estado.PENDIENTE);

        order = orderRepository.save(order);
        return new OrderResponse(order);
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

    @Transactional
    public OrderResponse updateOrderStatus(Long id, Order.Estado estado) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setEstado(estado);
        return new OrderResponse(orderRepository.save(order));
    }
}
