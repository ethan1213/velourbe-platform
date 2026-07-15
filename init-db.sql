-- Script de inicialización ejecutado por PostgreSQL al crear el contenedor.
-- Crea todas las bases de datos por servicio si no existen.

SELECT 'CREATE DATABASE db_scooter_users'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_users')\gexec

SELECT 'CREATE DATABASE db_scooter_rentals'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_rentals')\gexec

SELECT 'CREATE DATABASE db_scooter_payments'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_payments')\gexec

SELECT 'CREATE DATABASE db_scooter_notifications'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_notifications')\gexec

SELECT 'CREATE DATABASE db_scooter_analytics'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_analytics')\gexec

SELECT 'CREATE DATABASE db_scooter_logistics'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_logistics')\gexec

SELECT 'CREATE DATABASE db_scooter_maintenance'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_maintenance')\gexec

SELECT 'CREATE DATABASE db_scooter_support'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_scooter_support')\gexec
