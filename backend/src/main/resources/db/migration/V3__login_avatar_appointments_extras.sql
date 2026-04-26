-- V3: переименование username -> login, аватар, расширения для записей
-- (добавление поля login в users + поля для интеграций и переноса записи)

-- 1. Логин: переименование колонки username -> login.
ALTER TABLE users RENAME COLUMN username TO login;
ALTER INDEX idx_users_username RENAME TO idx_users_login;

-- 2. Аватар пользователя.
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(255);

-- 3. Расширения для интеграций и переноса записи.
ALTER TABLE appointments ADD COLUMN calendar_event_id VARCHAR(128);
ALTER TABLE appointments ADD COLUMN confirm_token     VARCHAR(64);
ALTER TABLE appointments ADD COLUMN reminder_sent_at  TIMESTAMP;
ALTER TABLE appointments ADD COLUMN reschedule_count  INTEGER NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX idx_appointments_confirm_token ON appointments(confirm_token);
