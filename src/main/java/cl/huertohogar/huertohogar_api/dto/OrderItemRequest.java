package cl.huertohogar.huertohogar_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull
    private String productoId;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    private Double precioUnitario;
}
