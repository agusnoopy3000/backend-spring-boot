-- Añadir columna firebase_uid para autenticación híbrida Firebase + JWT
ALTER TABLE users ADD COLUMN firebase_uid VARCHAR(128) UNIQUE;

-- Crear índice para búsquedas rápidas por firebase_uid
CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
