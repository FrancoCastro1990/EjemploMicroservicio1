-- Datos de prueba para eventos

INSERT INTO eventos (nombre, descripcion, fecha_evento, ubicacion, capacidad_total, precio_base)
SELECT 'Concierto Rock Nacional', 'Gran concierto de rock con las mejores bandas nacionales', '2025-03-15 20:00:00', 'Estadio Nacional, Santiago', 5000, 45000.00
WHERE NOT EXISTS (SELECT 1 FROM eventos WHERE nombre = 'Concierto Rock Nacional');

INSERT INTO eventos (nombre, descripcion, fecha_evento, ubicacion, capacidad_total, precio_base)
SELECT 'Festival de Jazz', 'Festival de jazz internacional con artistas de renombre', '2025-04-20 18:00:00', 'Teatro Caupolican, Santiago', 2000, 35000.00
WHERE NOT EXISTS (SELECT 1 FROM eventos WHERE nombre = 'Festival de Jazz');

INSERT INTO eventos (nombre, descripcion, fecha_evento, ubicacion, capacidad_total, precio_base)
SELECT 'Stand Up Comedy Night', 'Noche de comedia con los mejores comediantes del pais', '2025-02-28 21:00:00', 'Club de Comedia, Providencia', 300, 15000.00
WHERE NOT EXISTS (SELECT 1 FROM eventos WHERE nombre = 'Stand Up Comedy Night');

INSERT INTO eventos (nombre, descripcion, fecha_evento, ubicacion, capacidad_total, precio_base)
SELECT 'Feria Tecnologica 2025', 'Exposicion de las ultimas innovaciones tecnologicas', '2025-05-10 10:00:00', 'Espacio Riesco, Santiago', 10000, 5000.00
WHERE NOT EXISTS (SELECT 1 FROM eventos WHERE nombre = 'Feria Tecnologica 2025');

INSERT INTO eventos (nombre, descripcion, fecha_evento, ubicacion, capacidad_total, precio_base)
SELECT 'Obra de Teatro: Hamlet', 'Clasico de Shakespeare interpretado por actores nacionales', '2025-03-01 19:30:00', 'Teatro Municipal, Santiago', 800, 25000.00
WHERE NOT EXISTS (SELECT 1 FROM eventos WHERE nombre = 'Obra de Teatro: Hamlet');
