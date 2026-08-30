MERGE INTO mpa (id, name) KEY(id) VALUES
    (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17');

TRUNCATE TABLE genres RESTART IDENTITY;
INSERT INTO genres (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Мелодрама'), (6, 'Фантастика');