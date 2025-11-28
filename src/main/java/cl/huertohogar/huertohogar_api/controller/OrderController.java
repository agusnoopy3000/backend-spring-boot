package cl.huertohogar.huertohogar_api.controller;

import cl.huertohogar.huertohogar_api.dto.OrderRequest;
import cl.huertohogar.huertohogar_api.dto.OrderResponse;
import cl.huertohogar.huertohogar_api.model.OrderStatus;
import cl.huertohogar.huertohogar_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints para gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
        summary = "Crear nuevo pedido",
        description = "Crea un nuevo pedido para el usuario autenticado. El pedido se crea con estado PENDIENTE.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido creado exitosamente",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<OrderResponse> createOrder(
            @RequestAttribute("email") String email,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(email, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener pedido por ID",
        description = "Retorna los detalles de un pedido específico. Solo el propietario o un administrador pueden acceder.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - no es el propietario del pedido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            @RequestAttribute("email") String email,
            @RequestAttribute("role") String role) {
        OrderResponse order = orderService.getOrderById(id);
        // Check if user owns the order or is admin
        if (!order.getUser().getEmail().equals(email) && !"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(
        summary = "Obtener pedidos del usuario",
        description = "Retorna todos los pedidos del usuario autenticado. Los administradores pueden especificar un email para ver pedidos de otros usuarios.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestAttribute("email") String email,
            @RequestAttribute("role") String role,
            @Parameter(description = "Email del usuario (solo para administradores)")
            @RequestParam(value = "userEmail", required = false) String userEmail) {
        
        // Si es ADMIN y no especifica userEmail, devolver TODOS los pedidos
        if ("ADMIN".equalsIgnoreCase(role) && (userEmail == null || userEmail.isBlank())) {
            List<OrderResponse> allOrders = orderService.getAllOrders();
            return ResponseEntity.ok(allOrders);
        }
        
        String effectiveEmail = (userEmail != null && !userEmail.isBlank()) ? userEmail : email;
        if (userEmail != null && !userEmail.equalsIgnoreCase(email) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        List<OrderResponse> orders = orderService.getOrdersByUser(effectiveEmail);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar estado del pedido",
        description = "Actualiza el estado de un pedido. Solo accesible para administradores. Estados disponibles: PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    public ResponseEntity<OrderResponse> updateStatus(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest body) {
        OrderResponse updated = orderService.updateOrderStatus(id, body.getEstado());
        return ResponseEntity.ok(updated);
    }

    @Data
    @Schema(description = "Request para actualizar el estado de un pedido")
    private static class UpdateStatusRequest {
        @Schema(description = "Nuevo estado del pedido", example = "CONFIRMADO", allowableValues = {"PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO"})
        private OrderStatus estado;
    }

    @PutMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar mi pedido",
        description = "Permite al usuario cancelar su propio pedido. Solo se puede cancelar si el estado es PENDIENTE.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido cancelado exitosamente",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "El pedido no se puede cancelar (estado no es PENDIENTE)", content = @Content),
        @ApiResponse(responseCode = "403", description = "No es el propietario del pedido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    public ResponseEntity<OrderResponse> cancelMyOrder(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            @RequestAttribute("email") String email) {
        OrderResponse order = orderService.getOrderById(id);
        
        // Verificar que el usuario sea el propietario
        if (!order.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).build();
        }
        
        // Solo se puede cancelar si está PENDIENTE
        if (order.getEstado() != OrderStatus.PENDIENTE) {
            return ResponseEntity.badRequest().build();
        }
        
        OrderResponse cancelled = orderService.updateOrderStatus(id, OrderStatus.CANCELADO);
        return ResponseEntity.ok(cancelled);
    }
}
