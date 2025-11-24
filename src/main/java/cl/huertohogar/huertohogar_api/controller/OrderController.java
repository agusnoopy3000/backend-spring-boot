package cl.huertohogar.huertohogar_api.controller;

import cl.huertohogar.huertohogar_api.dto.OrderRequest;
import cl.huertohogar.huertohogar_api.dto.OrderResponse;
import cl.huertohogar.huertohogar_api.model.Order;
import cl.huertohogar.huertohogar_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(@RequestAttribute("email") String email, @Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(email, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id, @RequestAttribute("email") String email, @RequestAttribute("role") String role) {
        OrderResponse order = orderService.getOrderById(id);
        // Check if user owns the order or is admin
        if (!order.getUser().getEmail().equals(email) && !"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get orders for current user or by userEmail (admin)")
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestAttribute("email") String email,
                                                          @RequestAttribute("role") String role,
                                                          @RequestParam(value = "userEmail", required = false) String userEmail) {
        String effectiveEmail = (userEmail != null && !userEmail.isBlank()) ? userEmail : email;
        if (userEmail != null && !userEmail.equalsIgnoreCase(email) && !"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        List<OrderResponse> orders = orderService.getOrdersByUser(effectiveEmail);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @RequestAttribute("role") String role,
                                                       @RequestBody UpdateStatusRequest body) {
        if (!"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        OrderResponse updated = orderService.updateOrderStatus(id, body.getEstado());
        return ResponseEntity.ok(updated);
    }

    @Data
    private static class UpdateStatusRequest {
        private Order.Estado estado;
    }
}
