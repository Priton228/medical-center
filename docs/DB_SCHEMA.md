# Схема БД

Все таблицы создаются Flyway-миграциями (`backend/src/main/resources/db/migration/V1__init_schema.sql`). Уровень нормализации — 3НФ. Имена столбцов — snake_case, ключи — `id BIGSERIAL`, временные метки — `TIMESTAMP`.

## Сущности

### `roles`
| Поле | Тип | Описание |
|------|-----|----------|
| id | BIGSERIAL PK | |
| name | VARCHAR(32) UNIQUE | `ROLE_PATIENT` / `ROLE_DOCTOR` / `ROLE_ADMIN` |

### `users`
Учётная запись пользователя (общая для всех ролей).
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| username | VARCHAR(64) UNIQUE NOT NULL |
| email | VARCHAR(120) UNIQUE NOT NULL |
| password | VARCHAR(120) NOT NULL (BCrypt) |
| full_name | VARCHAR(120) NOT NULL |
| phone | VARCHAR(32) |
| enabled | BOOLEAN NOT NULL DEFAULT true |
| created_at | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP |

### `user_roles`
Связь many-to-many между `users` и `roles`.
| Поле | Тип |
|------|-----|
| user_id | BIGINT FK → users.id ON DELETE CASCADE |
| role_id | BIGINT FK → roles.id ON DELETE RESTRICT |
| PK | (user_id, role_id) |

### `patients`
Дополнительные данные пациента, 1:1 к `users`.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| user_id | BIGINT UNIQUE FK → users.id ON DELETE CASCADE |
| birth_date | DATE |
| address | VARCHAR(200) |
| insurance_number | VARCHAR(40) |

### `doctors`
Дополнительные данные врача, 1:1 к `users`.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| user_id | BIGINT UNIQUE FK → users.id ON DELETE CASCADE |
| specialization | VARCHAR(80) NOT NULL |
| bio | TEXT |
| photo_url | VARCHAR(255) |
| is_available | BOOLEAN NOT NULL DEFAULT true |
| work_start | TIME NOT NULL DEFAULT '09:00' |
| work_end | TIME NOT NULL DEFAULT '18:00' |
| room_number | VARCHAR(16) |

### `appointments`
Запись пациента на приём к врачу.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| patient_id | BIGINT FK → patients.id ON DELETE CASCADE |
| doctor_id | BIGINT FK → doctors.id ON DELETE RESTRICT |
| appointment_date | TIMESTAMP NOT NULL |
| status | VARCHAR(32) NOT NULL DEFAULT 'PLANNED' |
| notes | TEXT |
| created_at | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP |
| UNIQUE(doctor_id, appointment_date) | предотвращает двойную запись на один слот |

### `symptoms`
Справочник симптомов.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| name | VARCHAR(120) UNIQUE NOT NULL |
| description | TEXT |

### `diagnoses`
Справочник диагнозов с целевой специализацией.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| name | VARCHAR(160) UNIQUE NOT NULL |
| description | TEXT |
| specialization | VARCHAR(80) NOT NULL |

### `symptoms_diagnosis`
Связь many-to-many между симптомами и диагнозами с весом.
| Поле | Тип |
|------|-----|
| symptom_id | BIGINT FK → symptoms.id ON DELETE CASCADE |
| diagnosis_id | BIGINT FK → diagnoses.id ON DELETE CASCADE |
| weight | INT NOT NULL DEFAULT 1 |
| PK | (symptom_id, diagnosis_id) |

### `recommendations`
История анализов симптомов конкретного пациента.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| patient_id | BIGINT FK → patients.id ON DELETE CASCADE |
| symptoms_json | TEXT NOT NULL |
| diagnosis_id | BIGINT FK → diagnoses.id ON DELETE SET NULL |
| recommended_doctor_id | BIGINT FK → doctors.id ON DELETE SET NULL |
| confidence | INT DEFAULT 0 |
| created_at | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP |

### `medical_records`
Запись в медкарте по результату приёма.
| Поле | Тип |
|------|-----|
| id | BIGSERIAL PK |
| patient_id | BIGINT FK → patients.id ON DELETE CASCADE |
| doctor_id | BIGINT FK → doctors.id ON DELETE RESTRICT |
| appointment_id | BIGINT UNIQUE FK → appointments.id ON DELETE SET NULL |
| diagnosis | VARCHAR(255) NOT NULL |
| treatment | TEXT |
| notes | TEXT |
| created_at | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP |

## Связи

```
roles ─< user_roles >─ users ─1:1─ patients ─< appointments >─ doctors ─1:1─ users
                              └─1:1─ doctors                  └── medical_records ──┘
                                                              └── recommendations ──┘
symptoms ─< symptoms_diagnosis >─ diagnoses
                                  └─< recommendations
```

## Демо-данные (V2__seed_data.sql)

* 3 роли: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`.
* 6 пользователей с паролем **`password`**: `admin`, `doctor.ivanov`, `doctor.petrova`, `doctor.smirnov`, `patient.sidorov`, `patient.kozlova`.
* 3 врача (терапевт, кардиолог, невролог) и 2 пациента.
* 10 симптомов и 6 диагнозов с весовой матрицей `symptoms_diagnosis`.
* 2 демо-записи на приём.
