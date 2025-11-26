package cl.huertohogar.huertohogar_api.model;

/**
 * Estados posibles de un pedido
 */
public enum OrderStatus {
    PENDIENTE("Pendiente de confirmación"),
    CONFIRMADO("Confirmado por administrador"),
    ENVIADO("En camino al cliente"),
    ENTREGADO("Entregado al cliente"),
    CANCELADO("Pedido cancelado");

    private final String descripcion;

    OrderStatus(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si el estado es final (no puede cambiar)
     */
    public boolean isFinal() {
        return this == ENTREGADO || this == CANCELADO;
    }
}
