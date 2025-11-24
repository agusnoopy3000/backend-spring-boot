package cl.huertohogar.huertohogar_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Request para crear o actualizar un producto")
public class ProductRequest {

    @NotBlank
    @Schema(description = "Código único del producto", example = "VRD-001", required = true)
    private String codigo;

    @NotBlank
    @Schema(description = "Nombre del producto", example = "Tomate Cherry", required = true)
    private String nombre;

    @Schema(description = "Descripción detallada del producto", example = "Tomates cherry frescos de cultivo orgánico")
    private String descripcion;

    @NotNull
    @Positive
    @Schema(description = "Precio del producto en pesos chilenos", example = "2500.0", required = true)
    private Double precio;

    @NotNull
    @Positive
    @Schema(description = "Cantidad disponible en stock", example = "100", required = true)
    private Integer stock;

    @Schema(description = "URL de la imagen del producto", example = "https://example.com/images/tomate.jpg")
    private String imagen;

    @Schema(description = "Categoría del producto", example = "Verduras")
    private String categoria;
}
