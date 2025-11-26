-- V4__Create_documentos_table.sql
-- Crear tabla para almacenar metadatos de documentos en S3

CREATE TABLE IF NOT EXISTS documentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    url_publica VARCHAR(1000),
    user_email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_documentos_user_email (user_email),
    INDEX idx_documentos_s3_key (s3_key),
    
    CONSTRAINT fk_documentos_user 
        FOREIGN KEY (user_email) REFERENCES users(email)
        ON DELETE CASCADE
);
