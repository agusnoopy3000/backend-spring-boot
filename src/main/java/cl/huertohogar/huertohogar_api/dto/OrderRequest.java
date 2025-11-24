package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request para crear un pedido")
public class OrderRequest {

    @NotEmpty
    @Schema(description = "Lista de items del pedido (debe contener al menos un item)", required = true)
    private List<OrderItemRequest> items;
}
