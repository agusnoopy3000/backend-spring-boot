-- V6: Hacer que direccion y telefono sean opcionales para permitir registro sin estos campos
ALTER TABLE users MODIFY COLUMN direccion VARCHAR(255) NULL;
ALTER TABLE users MODIFY COLUMN telefono VARCHAR(50) NULL;
