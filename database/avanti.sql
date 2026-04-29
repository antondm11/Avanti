DROP DATABASE IF EXISTS avanti;
CREATE DATABASE avanti CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE avanti;


-- TABLAS

CREATE TABLE usuario (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    contrasena VARCHAR(128) NOT NULL,
    rol ENUM('admin','cliente') NOT NULL DEFAULT 'cliente',
    fecha_registro DATE NOT NULL DEFAULT (CURRENT_DATE)
);

CREATE TABLE genero (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE pelicula (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    director VARCHAR(128) NOT NULL,
    ano INT NOT NULL,
    sinopsis VARCHAR(600),
    duracion INT,
    precio DECIMAL(5,2) NOT NULL,
    imagen VARCHAR(255),
    video VARCHAR(255),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    id_genero INT NOT NULL,
    FOREIGN KEY (id_genero) REFERENCES genero(id) ON DELETE RESTRICT
);

CREATE TABLE tag (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE pelicula_tag (
    id_pelicula INT NOT NULL,
    id_tag INT NOT NULL,
    PRIMARY KEY (id_pelicula, id_tag),
    FOREIGN KEY (id_pelicula) REFERENCES pelicula(id) ON DELETE CASCADE,
    FOREIGN KEY (id_tag) REFERENCES tag(id) ON DELETE CASCADE
);

-- precio_pagado se asigna automáticamente por trigger
-- multa: 0.50 € por día de retraso, calculada por trigger
CREATE TABLE alquiler (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    fecha_alquiler DATE NOT NULL DEFAULT (CURRENT_DATE),
    fecha_devolucion DATE NOT NULL,
    CONSTRAINT chk_fechas_alquiler
        CHECK (
            fecha_devolucion >= fecha_alquiler
            AND fecha_devolucion <= fecha_alquiler + INTERVAL 21 DAY
        ),
    estado ENUM('ACTIVO','DEVUELTO','VENCIDO') NOT NULL DEFAULT 'ACTIVO',
    precio_pagado DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    multa DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    id_pelicula INT NOT NULL,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_pelicula) REFERENCES pelicula(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE RESTRICT
);

-- Índices para búsquedas e historial
CREATE INDEX idx_pelicula_titulo ON pelicula(titulo);
CREATE INDEX idx_pelicula_director ON pelicula(director);
CREATE INDEX idx_pelicula_ano ON pelicula(ano);
CREATE INDEX idx_alquiler_usuario ON alquiler(id_usuario);
CREATE INDEX idx_alquiler_estado ON alquiler(estado);


-- Roles gestionados desde Java mediante usuario.rol ENUM('admin','cliente')
-- Los usuarios MySQL y GRANT se configuran en el entorno de despliegue, no en el script


-- TRIGGERS

DELIMITER $$

-- ANTES de insertar un alquiler:
-- Asigna precio_pagado desde pelicula.precio (no hay que introducirlo a mano)
-- Marca la película como no disponible
CREATE TRIGGER trg_antes_alquiler
BEFORE INSERT ON alquiler
FOR EACH ROW
BEGIN
    SET NEW.precio_pagado = (SELECT precio FROM pelicula WHERE id = NEW.id_pelicula);
END$$

CREATE TRIGGER trg_despues_alquiler
AFTER INSERT ON alquiler
FOR EACH ROW
BEGIN
    IF NEW.estado IN ('ACTIVO', 'VENCIDO') THEN
        UPDATE pelicula SET disponible = FALSE WHERE id = NEW.id_pelicula;
    END IF;
END$$

-- ANTES de actualizar un alquiler: Si vence, cambia estado a VENCIDO y calcula multa (0.50€/día)
CREATE TRIGGER trg_antes_actualizar_alquiler
BEFORE UPDATE ON alquiler
FOR EACH ROW
BEGIN
    IF NEW.estado = 'ACTIVO' AND CURDATE() > NEW.fecha_devolucion THEN
        SET NEW.estado = 'VENCIDO';
    END IF;

    IF NEW.estado = 'VENCIDO' THEN
        SET NEW.multa = GREATEST(0, DATEDIFF(CURDATE(), NEW.fecha_devolucion)) * 0.50;
    END IF;
END$$

-- DESPUÉS de actualizar un alquiler: Si se devuelve, la película vuelve a estar disponible
CREATE TRIGGER trg_despues_actualizar_alquiler
AFTER UPDATE ON alquiler
FOR EACH ROW
BEGIN
    IF NEW.estado = 'DEVUELTO' AND OLD.estado != 'DEVUELTO' THEN
        UPDATE pelicula SET disponible = TRUE WHERE id = NEW.id_pelicula;
    END IF;
END$$

DELIMITER ;

-- La detección de vencidos se gestiona desde Java (DAO.checkAndUpdateVencidos())

-- VIEW: alquileres con multa recalculada al momento de consultar
-- Incluye id_usuario e id_pelicula para filtrar desde Java por ID

CREATE VIEW v_alquileres AS
SELECT
    a.id,
    a.id_usuario,
    a.id_pelicula,
    u.nombre AS usuario,
    p.titulo AS pelicula,
    a.fecha_alquiler,
    a.fecha_devolucion,
    a.estado,
    a.precio_pagado,
    a.multa,
    CASE
        WHEN CURDATE() > a.fecha_devolucion AND a.estado != 'DEVUELTO'
        THEN GREATEST(0, DATEDIFF(CURDATE(), a.fecha_devolucion)) * 0.50
        ELSE 0.00
    END AS multa_actual
FROM alquiler a
JOIN usuario u ON a.id_usuario = u.id
JOIN pelicula p ON a.id_pelicula = p.id;


-- DATOS DE PRUEBA

INSERT INTO genero (nombre) VALUES
('Drama'), ('Comedia'), ('Ciencia ficción'),('Acción'),('Bélico');

INSERT INTO pelicula (titulo, director, ano, sinopsis, duracion, precio, imagen, video, disponible, id_genero) VALUES
('El padrino', 'Francis Ford Coppola', 1972, 'Don Vito Corleone es el respetado y temido jefe de una de las cinco familias de la mafia de Nueva York en los años 40. El hombre tiene cuatro hijos: Connie, Sonny, Fredo y Michael, que no quiere saber nada de los negocios sucios de su padre. Cuando otro capo, Sollozzo, intenta asesinar a Corleone, empieza una cruenta lucha entre los distintos clanes.', 175, 5.99, 'url_imagen', 'url_video', TRUE, 1),
('Regreso al futuro', 'Robert Zemeckis', 1985, 'El adolescente Marty McFly es amigo de Doc., un científico que ha construido una máquina del tiempo. Cuando los dos prueban el artefacto, un error fortuito hace que Marty llegue a 1955, año en el que sus padres iban al instituto y todavía no se habían conocido. Después de impedir su primer encuentro, Marty deberá conseguir que se conozcan y se enamoren, de lo contrario su existencia no sería posible.', 116, 2.99, 'url_imagen', 'url_video', TRUE, 3),
('La extraña pareja', 'Gene Saks', 1968, 'Óscar y Félix se ven obligados a vivir juntos, pero sus personalidades y estilo de vida son tan diferentes que la convivencia es un desastre.', 105, 3.99, 'url_imagen', 'url_video', FALSE, 2),
('Casino', 'Martin Scorsese', 1995, 'En Las Vegas, en 1973, Sam Rothstein, es un profesional de las apuestas y director de un importante casino que pertenece a unos mafiosos. Un día, el violento Nicky Santoro, llega a las Vegas con unas crueles intenciones.', 173, 3.99, 'url_imagen', 'url_video', TRUE, 1),
('El imperio del sol', 'Steven Spielberg', 1987, 'Jim Graham es un niño inglés que, durante la Segunda Guerra Mundial, vive con sus padres en Shanghai. En 1941, los japoneses ocupan la ciudad y el pequeño es internado en un campo de concentración en el interior del país donde será cuidado por unos monjes.', 145, 2.99, 'url_imagen', 'url_video', FALSE, 3),
('Taxi Driver', 'Martin Scorsese', 1976, 'Travis Bickle, un veterano de la guerra del Vietnam, sufre de insomnio y trabaja como taxista nocturno en las calles de Nueva York. La ciudad, sucia, violenta y corrupta, lo perturba y lo consume.', 113, 3.99, 'url_imagen', 'url_video', TRUE, 1),
('El apartamento', 'Billy Wilder', 1960, 'Bud Baxter es un hombre solitario que trabaja en una compañía de seguros. Para ascender, presta su apartamento a sus jefes para sus encuentros amorosos.', 125, 3.99, 'url_imagen', 'url_video', TRUE, 2),
('A todo gas', 'Rob Cohen', 2001, 'El policía Brian O''Conner se infiltra en una banda de carreras ilegales y se debate entre su deber y su amistad con el líder del grupo.', 109, 3.99, 'url_imagen', 'url_video', FALSE, 4),
('Le Mans 66 ', 'James Mangold', 2019, 'En los años 60, Carroll Shelby y Ken Miles intentan construir un coche capaz de vencer a Ferrari en las 24 Horas de Le Mans.', 152, 4.99, 'url_imagen', 'url_video', TRUE, 1),
('Uno de los nuestros', 'Martin Scorsese', 1990, 'Henry, un niño de trece años de Brooklyn, vive fascinado con el mundo de los gánsteres. Su sueño se hace realidad cuando entra en la familia Pauline.', 148, 2.99, 'url_imagen', 'url_video', FALSE, 1),
('Misión Imposible', 'Brian De Palma', 1996, 'Ethan Hunt y su equipo, acusados de traición tras la misión en Praga, deben limpiar sus nombres mientras descubren una conspiración aún mayor que amenaza con revelarlos secretos de la CIA.', 169, 3.99, 'url_imagen', 'url_video', FALSE, 4),
('E.T. el Extraterrestre', 'Steven Spielberg', 1982, 'Elliot, un niño de ocho años, encuentra a E.T. , un extraterrestre perdido, y decide ayudarlo a volver a su planeta, mientras lo protege de los científicos del gobierno que quieren capturarlo.', 115, 4.99, 'url_imagen', 'url_video', FALSE, 3);

INSERT INTO tag (nombre) VALUES
('mafia'),('familia'),('crimen'),('clásico'),('soledad'),('ciudad'),('antihéroe'),('culto'),
('violencia'),('amistad'),('nostalgia'),('ambición'),('suspense'),('aventuras'),('histórica'),
('coches'),('extraterrestres'),('espionaje');

-- ID tags:
-- 1=mafia 2=familia 3=crimen 4=clásico 5=soledad 6=ciudad 7=antihéroe 8=culto
-- 9=violencia 10=amistad 11=nostalgia 12=ambición 13=suspense 14=aventuras
-- 15=histórica 16=coches 17=extraterrestres 18=espionaje

INSERT INTO pelicula_tag (id_pelicula, id_tag) VALUES
-- 1. El padrino
(1,1),(1,2),(1,3),(1,4),(1,9),(1,12),
-- 2. Regreso al futuro
(2,4),(2,10),(2,11),(2,14),
-- 3. La extraña pareja
(3,4),(3,10),(3,11),
-- 4. Casino
(4,1),(4,3),(4,9),(4,12),(4,13),
-- 5. El imperio del sol
(5,9),(5,11),(5,14),(5,15),
-- 6. Taxi Driver
(6,5),(6,6),(6,7),(6,8),(6,9),
-- 7. El apartamento
(7,4),(7,10),(7,11),
-- 8. A todo gas
(8,10),(8,13),(8,14),(8,16),
-- 9. Le Mans 66
(9,10),(9,15),(9,16),
-- 10. Uno de los nuestros
(10,1),(10,3),(10,8),(10,9),(10,12),
-- 11. Misión Imposible
(11,12),(11,13),(11,14),(11,18),
-- 12. E.T. el Extraterrestre
(12,4),(12,10),(12,11),(12,14),(12,17);

INSERT INTO usuario (nombre, email, contrasena, rol, fecha_registro) VALUES
('Admin Avanti', 'admin@avanti.com', 'admin123', 'admin', '2026-04-28'),
('Cliente Demo', 'cliente@avanti.com', 'cliente123', 'cliente', '2026-04-28'),
('Irene', 'irene@avanti.com', 'lastdinosaurs', 'cliente', '2026-04-28'),
('Anton', 'anton@avanti.com', 'fg0pg0482', 'cliente', '2026-04-24');

-- precio_pagado es sobreescrito por trg_antes_alquiler automáticamente
INSERT INTO alquiler (fecha_alquiler, fecha_devolucion, estado, id_pelicula, id_usuario) VALUES
('2026-04-14', '2026-04-28', 'DEVUELTO', 1, 2),
('2026-04-22', '2026-05-06', 'ACTIVO', 2, 2),
('2026-04-22', '2026-05-06', 'ACTIVO', 3, 3),
('2026-04-01', '2026-04-15', 'VENCIDO', 4, 4);


-- SELECTs de comprobación

SELECT * FROM usuario;
SELECT * FROM pelicula;
SELECT * FROM genero;
SELECT * FROM tag;
SELECT * FROM pelicula_tag;
SELECT * FROM alquiler;
SELECT * FROM v_alquileres;