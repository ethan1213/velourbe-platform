INSERT INTO users (email, password_hash, full_name, role, active)
VALUES (
    'admin@velourbe.cl',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFkUSC5Nv5FpVx9bkC5z.8K',
    'Administrador VeloUrbe',
    'ADMIN',
    TRUE
) ON CONFLICT (email) DO NOTHING;
