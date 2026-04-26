# Use cases

В соответствии с диаграммой пояснительной записки реализованы три актора: **Пациент**, **Врач**, **Администратор**. Ниже — что делает каждый из них и как это отражено в системе.

## Пациент (ROLE_PATIENT)

| Use case | Где в UI | Endpoint |
|----------|----------|----------|
| Регистрация / вход | `/register`, `/login` | `POST /auth/register`, `POST /auth/login` |
| Просмотр и редактирование профиля | `/patient/profile` | `GET/PUT /patients/me`, `PUT /users/me` |
| Поиск врачей и просмотр расписания | `/patient/doctors` | `GET /doctors`, `GET /doctors/{id}` |
| Запись на приём | модальное окно из `/patient/doctors` | `POST /appointments` |
| Список своих записей и отмена | `/patient/appointments` | `GET /appointments/me`, `PATCH /appointments/{id}/status` |
| **Анализ симптомов и рекомендация врача** | `/patient/symptoms` | `POST /recommendations/analyze`, `GET /recommendations/me` |
| Медкарта (история приёмов) | `/patient/records` | `GET /medical-records/me` |

## Врач (ROLE_DOCTOR)

| Use case | UI | Endpoint |
|----------|----|----------|
| Вход в кабинет | `/login` | `POST /auth/login` |
| Просмотр расписания и доступности | `/doctor` | `GET /doctors/me` |
| Управление часами приёма | `/doctor/schedule` | `PUT /doctors/me/schedule` |
| Просмотр предстоящих и прошедших приёмов | `/doctor/appointments` | `GET /appointments/me`, `/appointments/me/upcoming` |
| Подтверждение / отмена записи | `/doctor/appointments` | `PATCH /appointments/{id}/status` |
| Завершение приёма с заполнением медкарты | `/doctor/appointments` (модал) | `POST /medical-records` + `PATCH /appointments/{id}/status=COMPLETED` |
| История заполненных медкарт | `/doctor/records` | `GET /medical-records/by-doctor` |

## Администратор (ROLE_ADMIN)

| Use case | UI | Endpoint |
|----------|----|----------|
| Дашборд со статистикой | `/admin` | `GET /admin/stats/overview` |
| Управление пользователями (вкл/выкл) | `/admin/users` | `GET /users`, `PATCH /users/{id}/enabled` |
| Создание/удаление врачей | `/admin/doctors` | `POST/PUT/DELETE /doctors` |
| Просмотр пациентов | `/admin/patients` | `GET /patients` |
| CRUD справочников симптомов | `/admin/symptoms` | `GET/POST/PUT/DELETE /symptoms` |
| CRUD диагнозов с привязкой к симптомам | `/admin/diagnoses` | `GET/POST/PUT/DELETE /diagnoses` |

## Покрытие основных сценариев записки

* **Запись пациента к врачу по специализации** — реализовано через `/patient/doctors` (фильтрация) и `POST /appointments` с проверкой занятости слота (UNIQUE doctor_id+appointment_date).
* **Подбор врача по симптомам** — модуль `RecommendationService` (Strategy + Facade), сохранение истории в `recommendations`.
* **Управление расписанием врача** — `PUT /doctors/me/schedule` (work_start/end/available).
* **Ведение медкарты** — `MedicalRecordController`, доступ только врачу-владельцу записи или администратору.
* **Аналитика для администратора** — `StatsService` отдает агрегированные показатели для дашборда.
