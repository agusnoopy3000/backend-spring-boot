package cl.huertohogar.huertohogar_api.dto;

import cl.huertohogar.huertohogar_api.model.OrderItem;
import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private String productoId;
    private Integer cantidad;
    private Double precioUnitario;

    public OrderItemResponse(OrderItem item) {
        this.id = item.getId();
        this.productoId = item.getProductoId();
        this.cantidad = item.getCantidad();
        this.precioUnitario = item.getPrecioUnitario();
    }
}
