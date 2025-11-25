-- V2__Insert_sample_data.sql
-- Datos de prueba para Huerto Hogar

-- =============================================
-- USUARIOS
-- =============================================
-- Password: Admin123! (encriptado con BCrypt)
INSERT INTO users (email, nombre, apellido, run, password, direccion, telefono, rol) VALUES
('admin@huertohogar.cl', 'Administrador', 'Sistema', '11.111.111-1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqZ3jIk0V8U5Q6xT.k5a5e5Q5e5Q5', 'Av. Providencia 1234, Santiago', '+56911111111', 'ADMIN'),
('juan.perez@gmail.com', 'Juan', 'Pérez González', '12.345.678-9', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqZ3jIk0V8U5Q6xT.k5a5e5Q5e5Q5', 'Los Aromos 567, Ñuñoa', '+56922222222', 'USER'),
('maria.garcia@gmail.com', 'María', 'García López', '13.456.789-0', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqZ3jIk0V8U5Q6xT.k5a5e5Q5e5Q5', 'Las Violetas 890, Las Condes', '+56933333333', 'USER'),
('pedro.soto@gmail.com', 'Pedro', 'Soto Muñoz', '14.567.890-1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqZ3jIk0V8U5Q6xT.k5a5e5Q5e5Q5', 'El Roble 123, Providencia', '+56944444444', 'USER'),
('ana.martinez@gmail.com', 'Ana', 'Martínez Silva', '15.678.901-2', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqZ3jIk0V8U5Q6xT.k5a5e5Q5e5Q5', 'Los Pinos 456, Vitacura', '+56955555555', 'USER');

-- =============================================
-- PRODUCTOS - Verduras y Hortalizas
-- =============================================
INSERT INTO products (codigo, nombre, descripcion, precio, stock, imagen, categoria) VALUES
-- Verduras de hoja
('VH-001', 'Lechuga Costina', 'Lechuga costina fresca, cultivada orgánicamente. Ideal para ensaladas.', 1500, 100, 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400', 'Verduras de Hoja'),
('VH-002', 'Espinaca', 'Espinaca fresca en atado. Rica en hierro y vitaminas.', 1800, 80, 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400', 'Verduras de Hoja'),
('VH-003', 'Acelga', 'Acelga orgánica en atado. Perfecta para guisos y salteados.', 1600, 60, 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400', 'Verduras de Hoja'),
('VH-004', 'Rúcula', 'Rúcula fresca con sabor ligeramente picante. Para ensaladas gourmet.', 2000, 50, 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400', 'Verduras de Hoja'),
('VH-005', 'Kale', 'Kale o col rizada. Superfood rico en antioxidantes.', 2200, 40, 'https://images.unsplash.com/photo-1524179091875-bf99a9a6af57?w=400', 'Verduras de Hoja'),

-- Tomates
('TM-001', 'Tomate Cherry', 'Tomates cherry dulces y jugosos. Bandeja 250g.', 2500, 120, 'https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400', 'Tomates'),
('TM-002', 'Tomate Limachino', 'Tomate limachino tradicional. Por kilo.', 2800, 90, 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400', 'Tomates'),
('TM-003', 'Tomate Racimo', 'Tomates en racimo, madurados en planta.', 3000, 70, 'https://images.unsplash.com/photo-1582284540020-8acbe03f4924?w=400', 'Tomates'),

-- Raíces y Tubérculos
('RT-001', 'Zanahoria', 'Zanahorias orgánicas, paquete de 1kg.', 1800, 150, 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400', 'Raíces'),
('RT-002', 'Betarraga', 'Betarragas frescas, ideales para ensaladas y jugos.', 2000, 80, 'https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?w=400', 'Raíces'),
('RT-003', 'Rabanito', 'Rabanitos frescos en atado. Crujientes y picantes.', 1500, 60, 'https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=400', 'Raíces'),
('RT-004', 'Papa Chiloé', 'Papas nativas de Chiloé, 1kg.', 2500, 100, 'https://images.unsplash.com/photo-1508313880080-c4bef0730395?w=400', 'Raíces'),

-- Hierbas Aromáticas
('HA-001', 'Albahaca', 'Albahaca fresca en maceta pequeña.', 1200, 45, 'https://images.unsplash.com/photo-1618375569909-3c8616cf7733?w=400', 'Hierbas'),
('HA-002', 'Cilantro', 'Cilantro fresco en atado.', 800, 100, 'https://images.unsplash.com/photo-1526318472351-c75fcf070305?w=400', 'Hierbas'),
('HA-003', 'Perejil', 'Perejil liso fresco, atado grande.', 800, 90, 'https://images.unsplash.com/photo-1588165171080-c89acfa5ee83?w=400', 'Hierbas'),
('HA-004', 'Menta', 'Menta fresca en atado. Ideal para bebidas y postres.', 1000, 55, 'https://images.unsplash.com/photo-1571027200367-1e48cc3b93de?w=400', 'Hierbas'),
('HA-005', 'Romero', 'Romero fresco en rama.', 1000, 50, 'https://images.unsplash.com/photo-1515586000433-45406d8e6662?w=400', 'Hierbas'),

-- Otros Vegetales
('OV-001', 'Zapallo Italiano', 'Zapallo italiano (zucchini) fresco. Por unidad.', 1200, 80, 'https://images.unsplash.com/photo-1596363505729-4190a9506133?w=400', 'Otros'),
('OV-002', 'Pepino', 'Pepino fresco, ideal para ensaladas.', 1000, 70, 'https://images.unsplash.com/photo-1449300079323-02e209d9d3a6?w=400', 'Otros'),
('OV-003', 'Pimentón Rojo', 'Pimentón rojo dulce. Por unidad.', 1500, 90, 'https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400', 'Otros'),
('OV-004', 'Pimentón Verde', 'Pimentón verde fresco. Por unidad.', 1200, 85, 'https://images.unsplash.com/photo-1518736114810-3f3bedfec66a?w=400', 'Otros'),
('OV-005', 'Berenjena', 'Berenjena orgánica. Por unidad.', 1800, 45, 'https://images.unsplash.com/photo-1528505086635-4c69d5f10908?w=400', 'Otros'),
('OV-006', 'Choclo', 'Choclo tierno, unidad.', 800, 120, 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=400', 'Otros'),
('OV-007', 'Poroto Verde', 'Porotos verdes frescos, 500g.', 2200, 55, 'https://images.unsplash.com/photo-1567375698348-5d9d5ae99de0?w=400', 'Otros');

-- =============================================
-- PEDIDOS DE EJEMPLO
-- =============================================
INSERT INTO orders (user_email, total, estado) VALUES
('juan.perez@gmail.com', 8500, 'PENDIENTE'),
('juan.perez@gmail.com', 12300, 'ENTREGADO'),
('maria.garcia@gmail.com', 6800, 'CONFIRMADO'),
('pedro.soto@gmail.com', 15600, 'ENVIADO'),
('ana.martinez@gmail.com', 4500, 'PENDIENTE');

-- =============================================
-- ITEMS DE PEDIDOS
-- =============================================
-- Pedido 1: Juan - Pendiente
INSERT INTO order_items (order_id, producto_id, cantidad, precio_unitario) VALUES
(1, 'VH-001', 2, 1500),
(1, 'TM-001', 1, 2500),
(1, 'HA-002', 3, 800);

-- Pedido 2: Juan - Entregado
INSERT INTO order_items (order_id, producto_id, cantidad, precio_unitario) VALUES
(2, 'VH-002', 2, 1800),
(2, 'RT-001', 3, 1800),
(2, 'OV-003', 2, 1500);

-- Pedido 3: María - Confirmado
INSERT INTO order_items (order_id, producto_id, cantidad, precio_unitario) VALUES
(3, 'VH-005', 1, 2200),
(3, 'TM-002', 1, 2800),
(3, 'HA-001', 1, 1200);

-- Pedido 4: Pedro - Enviado
INSERT INTO order_items (order_id, producto_id, cantidad, precio_unitario) VALUES
(4, 'RT-004', 3, 2500),
(4, 'OV-001', 4, 1200),
(4, 'OV-006', 5, 800);

-- Pedido 5: Ana - Pendiente
INSERT INTO order_items (order_id, producto_id, cantidad, precio_unitario) VALUES
(5, 'HA-003', 2, 800),
(5, 'HA-004', 1, 1000),
(5, 'VH-001', 1, 1500);
