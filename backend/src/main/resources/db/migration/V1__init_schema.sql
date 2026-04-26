-- Medical Center: initial schema (V1)
-- 11 связанных таблиц, приведённых к 3НФ.

CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(64) NOT NULL UNIQUE,
    email      VARCHAR(128) NOT NULL UNIQUE,
    password   VARCHAR(128) NOT NULL,
    full_name  VARCHAR(128) NOT NULL,
    phone      VARCHAR(32),
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE patients (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    birth_date       DATE,
    address          VARCHAR(255),
    insurance_number VARCHAR(64)
);

CREATE TABLE doctors (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialization VARCHAR(128) NOT NULL,
    bio            TEXT,
    photo_url      VARCHAR(255),
    is_available   BOOLEAN NOT NULL DEFAULT TRUE,
    work_start     TIME NOT NULL DEFAULT '09:00',
    work_end       TIME NOT NULL DEFAULT '18:00',
    room_number    VARCHAR(16)
);
CREATE INDEX idx_doctors_specialization ON doctors(specialization);

CREATE TABLE appointments (
    id               BIGSERIAL PRIMARY KEY,
    patient_id       BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id        BIGINT NOT NULL REFERENCES doctors(id) ON DELETE RESTRICT,
    appointment_date TIMESTAMP NOT NULL,
    status           VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    notes            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, appointment_date)
);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);

CREATE TABLE symptoms (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE diagnoses (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(128) NOT NULL UNIQUE,
    description     TEXT,
    specialization  VARCHAR(128) NOT NULL
);
CREATE INDEX idx_diagnoses_specialization ON diagnoses(specialization);

CREATE TABLE symptoms_diagnosis (
    symptom_id   BIGINT NOT NULL REFERENCES symptoms(id) ON DELETE CASCADE,
    diagnosis_id BIGINT NOT NULL REFERENCES diagnoses(id) ON DELETE CASCADE,
    weight       INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (symptom_id, diagnosis_id)
);

CREATE TABLE recommendations (
    id                    BIGSERIAL PRIMARY KEY,
    patient_id            BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    symptoms_json         TEXT NOT NULL,
    diagnosis_id          BIGINT REFERENCES diagnoses(id) ON DELETE SET NULL,
    recommended_doctor_id BIGINT REFERENCES doctors(id) ON DELETE SET NULL,
    confidence            INTEGER NOT NULL DEFAULT 0,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_recommendations_patient ON recommendations(patient_id);

CREATE TABLE medical_records (
    id             BIGSERIAL PRIMARY KEY,
    patient_id     BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id      BIGINT NOT NULL REFERENCES doctors(id) ON DELETE RESTRICT,
    appointment_id BIGINT UNIQUE REFERENCES appointments(id) ON DELETE SET NULL,
    diagnosis      VARCHAR(255) NOT NULL,
    treatment      TEXT,
    notes          TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_records_patient ON medical_records(patient_id);
CREATE INDEX idx_records_doctor ON medical_records(doctor_id);
