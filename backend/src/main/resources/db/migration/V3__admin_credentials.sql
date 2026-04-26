-- По требованию заказчика: пароль администратора по умолчанию = 'admin'.
-- BCrypt-хэш для 'admin' (cost=10).
UPDATE users
SET password = '$2b$10$alpfJ1aPFvJo9zwwhS324OEsc7lG51vQxtjU7BztCjF2OlCUhs2RS'
WHERE username = 'admin';

-- На случай чистой БД: создаём админа admin/admin, если его ещё нет.
INSERT INTO users (username, email, password, full_name, phone, enabled)
SELECT 'admin', 'admin@medcenter.local',
       '$2b$10$alpfJ1aPFvJo9zwwhS324OEsc7lG51vQxtjU7BztCjF2OlCUhs2RS',
       'Администратор Системы', '+375290000000', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
