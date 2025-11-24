package cl.huertohogar.huertohogar_api.dto;

import cl.huertohogar.huertohogar_api.model.Order;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private Long id;
    private UserResponse user;
    private List<OrderItemResponse> items;
    private Double total; // changed from BigDecimal
    private Order.Estado estado;
    private LocalDateTime createdAt;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.user = new UserResponse(order.getUser());
        this.items = order.getItems().stream().map(OrderItemResponse::new).toList();
        this.total = order.getTotal();
        this.estado = order.getEstado();
        this.createdAt = order.getCreatedAt();
    }
}
