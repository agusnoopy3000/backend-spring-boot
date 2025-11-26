package cl.huertohogar.huertohogar_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus estado;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // NUEVOS CAMPOS PARA DATOS DE ENTREGA
    @Column(name = "direccion_entrega")
    private String direccionEntrega;

    @Column(name = "region")
    private String region;

    @Column(name = "comuna")
    private String comuna;

    @Column(name = "comentarios")
    private String comentarios;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;
}
