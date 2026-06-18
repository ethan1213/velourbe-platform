-- Script de inicialización ejecutado por PostgreSQL al crear el contenedor.
-- Crea ambas bases de datos si no existen.

SELECT 'CREATE DATABASE db_scooter_users'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_users')\gexec

SELECT 'CREATE DATABASE db_scooter_rentals'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_rentals')\gexec
