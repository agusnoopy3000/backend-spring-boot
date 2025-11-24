package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Item de un pedido")
public class OrderItemRequest {

    @NotNull
    @Schema(description = "Código del producto (campo 'codigo' en Product)", example = "VRD-001")
    private String productoId;

    @NotNull
    @Positive
    @Schema(description = "Cantidad de unidades del producto", example = "5")
    private Integer cantidad;
}
