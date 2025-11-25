package cl.huertohogar.huertohogar_api.controller;

import cl.huertohogar.huertohogar_api.dto.ProductRequest;
import cl.huertohogar.huertohogar_api.dto.ProductResponse;
import cl.huertohogar.huertohogar_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints para gestión de productos (verduras y hortalizas)")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
        summary = "Obtener todos los productos",
        description = "Retorna una lista de todos los productos disponibles. Opcionalmente se puede filtrar por búsqueda."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
    })
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @Parameter(description = "Término de búsqueda para filtrar productos por nombre o descripción")
            @RequestParam(required = false) String q) {
        List<ProductResponse> products = productService.getAllProducts(q);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID",
        description = "Retorna los detalles de un producto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(
        summary = "Obtener producto por código",
        description = "Retorna los detalles de un producto específico usando su código único (ej: VH-001)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<ProductResponse> getProductByCodigo(
            @Parameter(description = "Código del producto (ej: VH-001)", required = true)
            @PathVariable String codigo) {
        ProductResponse product = productService.getProductByCodigo(codigo);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear nuevo producto",
        description = "Crea un nuevo producto en el catálogo. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto creado exitosamente",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o código de producto duplicado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content)
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza la información de un producto existente. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content)
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID del producto a actualizar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del catálogo. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content)
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto a eliminar", required = true)
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
