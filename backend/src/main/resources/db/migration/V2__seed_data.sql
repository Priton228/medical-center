-- Demo data. All passwords are 'password' (BCrypt).
-- BCrypt hash for 'password': $2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS

INSERT INTO roles (name) VALUES ('ROLE_PATIENT'), ('ROLE_DOCTOR'), ('ROLE_ADMIN');

-- Users
INSERT INTO users (username, email, password, full_name, phone) VALUES
  ('admin',           'admin@medcenter.local',       '$2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS', 'Администратор Системы',     '+375290000000'),
  ('doctor.ivanov',   'ivanov@medcenter.local',      '$2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS', 'Иванов Иван Иванович',      '+375291110001'),
  ('doctor.petrova',  'petrova@medcenter.local',     '$2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS', 'Петрова Анна Сергеевна',    '+375291110002'),
  ('doctor.smirnov',  'smirnov@medcenter.local',     '$2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS', 'Смирнов Андрей Олегович',   '+375291110003'),
  ('patient.sidorov', 'sidorov@example.com',         '$2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS', 'Сидоров Пётр Алексеевич',   '+375292220001'),
  ('patient.kozlova', 'kozlova@example.com',         '$2b$10$aLD/XijugHX2lctWOcCbzeFoWd3.63fUgRoLgavX7SpGSOXqcj9cS', 'Козлова Мария Викторовна',  '+375292220002');

INSERT INTO user_roles (user_id, role_id) VALUES
  ((SELECT id FROM users WHERE username='admin'),           (SELECT id FROM roles WHERE name='ROLE_ADMIN')),
  ((SELECT id FROM users WHERE username='doctor.ivanov'),   (SELECT id FROM roles WHERE name='ROLE_DOCTOR')),
  ((SELECT id FROM users WHERE username='doctor.petrova'),  (SELECT id FROM roles WHERE name='ROLE_DOCTOR')),
  ((SELECT id FROM users WHERE username='doctor.smirnov'),  (SELECT id FROM roles WHERE name='ROLE_DOCTOR')),
  ((SELECT id FROM users WHERE username='patient.sidorov'), (SELECT id FROM roles WHERE name='ROLE_PATIENT')),
  ((SELECT id FROM users WHERE username='patient.kozlova'), (SELECT id FROM roles WHERE name='ROLE_PATIENT'));

INSERT INTO doctors (user_id, specialization, bio, is_available, work_start, work_end, room_number) VALUES
  ((SELECT id FROM users WHERE username='doctor.ivanov'),  'Терапевт',  'Врач-терапевт высшей категории, стаж 12 лет.',  TRUE, '09:00', '17:00', '101'),
  ((SELECT id FROM users WHERE username='doctor.petrova'), 'Кардиолог', 'Кардиолог, кандидат медицинских наук, стаж 9 лет.', TRUE, '10:00', '18:00', '204'),
  ((SELECT id FROM users WHERE username='doctor.smirnov'), 'Невролог',  'Врач-невролог, стаж 7 лет.',                   TRUE, '08:30', '16:30', '305');

INSERT INTO patients (user_id, birth_date, address, insurance_number) VALUES
  ((SELECT id FROM users WHERE username='patient.sidorov'), DATE '1990-05-14', 'г. Минск, ул. Независимости, 12',  'BY-123-456-789'),
  ((SELECT id FROM users WHERE username='patient.kozlova'), DATE '1985-11-02', 'г. Минск, пр. Победителей, 80',    'BY-987-654-321');

INSERT INTO symptoms (name, description) VALUES
  ('Головная боль',    'Боль в области головы различной интенсивности.'),
  ('Повышенная температура', 'Температура тела выше 37,2 °C.'),
  ('Кашель',           'Сухой или влажный кашель.'),
  ('Боль в груди',     'Дискомфорт или боль в грудной клетке.'),
  ('Одышка',           'Затруднённое дыхание.'),
  ('Головокружение',   'Чувство потери равновесия.'),
  ('Слабость',         'Общая слабость, утомляемость.'),
  ('Боль в животе',    'Боль или дискомфорт в области живота.'),
  ('Онемение конечностей', 'Снижение или потеря чувствительности.'),
  ('Бессонница',       'Нарушение сна.');

INSERT INTO diagnoses (name, description, specialization) VALUES
  ('ОРВИ',                 'Острая респираторная вирусная инфекция.',     'Терапевт'),
  ('Артериальная гипертензия', 'Повышенное артериальное давление.',       'Кардиолог'),
  ('Мигрень',              'Хроническое неврологическое заболевание.',    'Невролог'),
  ('Ишемическая болезнь сердца', 'Хроническая болезнь сердца.',           'Кардиолог'),
  ('Гастрит',              'Воспаление слизистой желудка.',               'Терапевт'),
  ('Невралгия',            'Поражение периферических нервов.',            'Невролог');

INSERT INTO symptoms_diagnosis (symptom_id, diagnosis_id, weight) VALUES
  ((SELECT id FROM symptoms WHERE name='Повышенная температура'), (SELECT id FROM diagnoses WHERE name='ОРВИ'), 3),
  ((SELECT id FROM symptoms WHERE name='Кашель'),                 (SELECT id FROM diagnoses WHERE name='ОРВИ'), 3),
  ((SELECT id FROM symptoms WHERE name='Слабость'),               (SELECT id FROM diagnoses WHERE name='ОРВИ'), 1),
  ((SELECT id FROM symptoms WHERE name='Головная боль'),          (SELECT id FROM diagnoses WHERE name='Мигрень'), 3),
  ((SELECT id FROM symptoms WHERE name='Головокружение'),         (SELECT id FROM diagnoses WHERE name='Мигрень'), 2),
  ((SELECT id FROM symptoms WHERE name='Бессонница'),             (SELECT id FROM diagnoses WHERE name='Мигрень'), 1),
  ((SELECT id FROM symptoms WHERE name='Боль в груди'),           (SELECT id FROM diagnoses WHERE name='Артериальная гипертензия'), 2),
  ((SELECT id FROM symptoms WHERE name='Головная боль'),          (SELECT id FROM diagnoses WHERE name='Артериальная гипертензия'), 2),
  ((SELECT id FROM symptoms WHERE name='Одышка'),                 (SELECT id FROM diagnoses WHERE name='Артериальная гипертензия'), 1),
  ((SELECT id FROM symptoms WHERE name='Боль в груди'),           (SELECT id FROM diagnoses WHERE name='Ишемическая болезнь сердца'), 3),
  ((SELECT id FROM symptoms WHERE name='Одышка'),                 (SELECT id FROM diagnoses WHERE name='Ишемическая болезнь сердца'), 2),
  ((SELECT id FROM symptoms WHERE name='Слабость'),               (SELECT id FROM diagnoses WHERE name='Ишемическая болезнь сердца'), 1),
  ((SELECT id FROM symptoms WHERE name='Боль в животе'),          (SELECT id FROM diagnoses WHERE name='Гастрит'), 3),
  ((SELECT id FROM symptoms WHERE name='Слабость'),               (SELECT id FROM diagnoses WHERE name='Гастрит'), 1),
  ((SELECT id FROM symptoms WHERE name='Онемение конечностей'),   (SELECT id FROM diagnoses WHERE name='Невралгия'), 3),
  ((SELECT id FROM symptoms WHERE name='Головокружение'),         (SELECT id FROM diagnoses WHERE name='Невралгия'), 1);

-- A couple of demo appointments
INSERT INTO appointments (patient_id, doctor_id, appointment_date, status, notes) VALUES
  ((SELECT id FROM patients WHERE user_id=(SELECT id FROM users WHERE username='patient.sidorov')),
   (SELECT id FROM doctors  WHERE user_id=(SELECT id FROM users WHERE username='doctor.ivanov')),
   CURRENT_TIMESTAMP + INTERVAL '2 day',  'PLANNED',  'Плановый осмотр'),
  ((SELECT id FROM patients WHERE user_id=(SELECT id FROM users WHERE username='patient.kozlova')),
   (SELECT id FROM doctors  WHERE user_id=(SELECT id FROM users WHERE username='doctor.petrova')),
   CURRENT_TIMESTAMP + INTERVAL '3 day',  'PLANNED',  'Жалобы на давление');
