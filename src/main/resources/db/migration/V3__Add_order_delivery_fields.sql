-- V3__Add_order_delivery_fields.sql
-- Agregar campos de entrega a la tabla orders

ALTER TABLE orders ADD COLUMN direccion_entrega VARCHAR(500);
ALTER TABLE orders ADD COLUMN region VARCHAR(100);
ALTER TABLE orders ADD COLUMN comuna VARCHAR(100);
ALTER TABLE orders ADD COLUMN comentarios TEXT;
ALTER TABLE orders ADD COLUMN fecha_entrega DATE;
