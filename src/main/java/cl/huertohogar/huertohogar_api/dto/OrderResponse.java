package cl.huertohogar.huertohogar_api.dto;

import cl.huertohogar.huertohogar_api.model.Order;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private Long id;
    private UserResponse user;
    private List<OrderItemResponse> items;
    private Double total;
    private Order.Estado estado;
    private String direccionEntrega;
    private String region;
    private String comuna;
    private String comentarios;
    private LocalDate fechaEntrega;
    private LocalDateTime createdAt;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.user = new UserResponse(order.getUser());
        this.items = order.getItems().stream().map(OrderItemResponse::new).toList();
        this.total = order.getTotal();
        this.estado = order.getEstado();
        this.direccionEntrega = order.getDireccionEntrega();
        this.region = order.getRegion();
        this.comuna = order.getComuna();
        this.comentarios = order.getComentarios();
        this.fechaEntrega = order.getFechaEntrega();
        this.createdAt = order.getCreatedAt();
    }
}
