package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Item de un pedido")
public class OrderItemRequest {

    @NotNull
    @Schema(description = "ID del producto", example = "VRD-001", required = true)
    private String productoId;

    @NotNull
    @Positive
    @Schema(description = "Cantidad de unidades del producto", example = "5", required = true)
    private Integer cantidad;

    @NotNull
    @Schema(description = "Precio unitario del producto al momento del pedido", example = "2500.0", required = true)
    private Double precioUnitario;
}
