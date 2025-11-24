package cl.huertohogar.huertohogar_api.dto;

import cl.huertohogar.huertohogar_api.model.Product;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private String id; // expose as String
    private String codigo;
    private String nombre;
    private String descripcion;
    private Double precio; // changed from BigDecimal
    private Integer stock;
    private String imagen;
    private String categoria;
    private LocalDateTime createdAt;

    public ProductResponse(Product product) {
        this.id = product.getId() != null ? product.getId().toString() : null;
        this.codigo = product.getCodigo();
        this.nombre = product.getNombre();
        this.descripcion = product.getDescripcion();
        this.precio = product.getPrecio();
        this.stock = product.getStock();
        this.imagen = product.getImagen();
        this.categoria = product.getCategoria();
        this.createdAt = null; // No createdAt field on Product entity
    }
}
