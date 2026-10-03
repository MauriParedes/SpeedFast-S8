CREATE DATABASE IF NOT EXISTS speedfast_db;
USE speedfast_db;

DROP TABLE IF EXISTS entregas;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS repartidores;

CREATE TABLE repartidores (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL
);

CREATE TABLE pedidos (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         direccion VARCHAR(100) NOT NULL,
                         tipo ENUM('COMIDA','ENCOMIENDA','EXPRESS'),
                         estado ENUM('PENDIENTE','EN_REPARTO','ENTREGADO')
);

CREATE TABLE entregas (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          id_pedido INT,
                          id_repartidor INT,
                          fecha DATE,
                          hora TIME,
                          FOREIGN KEY (id_pedido) REFERENCES pedidos(id),
                          FOREIGN KEY (id_repartidor) REFERENCES repartidores(id)
);

-- Datos de prueba
INSERT INTO repartidores (nombre) VALUES ('Mauricio Paredes'), ('Valentina Arenas'), ('Bart Simpson');

INSERT INTO pedidos (direccion, tipo, estado) VALUES
                                                  ('Paula Jaraquemada 083, Quilicura ', 'COMIDA', 'EN_REPARTO'),
                                                  ('Luis Matte Larrain 1168, Puente Alto', 'ENCOMIENDA', 'PENDIENTE'),
                                                  ('Avenida Siempreviva 472, Springfield', 'EXPRESS', 'ENTREGADO');


INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (1, 1, CURDATE(), CURTIME());

SELECT * FROM repartidores;
SELECT * FROM pedidos;
SELECT * FROM entregas;