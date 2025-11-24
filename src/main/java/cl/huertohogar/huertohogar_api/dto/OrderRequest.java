package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request para crear un pedido")
public class OrderRequest {

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Schema(description = "Dirección donde se entregará el pedido", example = "Lamarck 36, Valparaíso")
    private String direccionEntrega;

    @FutureOrPresent(message = "La fecha de entrega debe ser hoy o una fecha futura")
    @Schema(description = "Fecha preferida de entrega (formato ISO yyyy-MM-dd)", example = "2025-11-07")
    private LocalDate fechaEntrega;

    @Schema(description = "Región de entrega", example = "Region de Valparaiso")
    private String region;

    @Schema(description = "Comuna de entrega", example = "Valparaíso")
    private String comuna;

    @Schema(description = "Comentarios adicionales del cliente", example = "Dejar en conserjería")
    private String comentarios;

    @NotEmpty(message = "Debe incluir al menos un item en el pedido")
    @Schema(description = "Lista de items del pedido (debe contener al menos un item)")
    private List<OrderItemRequest> items;
}
