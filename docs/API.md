# REST API

База: `http://localhost:8080`. Все ответы — JSON. Аутентификация — `Authorization: Bearer <jwt>`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Auth

| Method | Path | Auth | Описание |
|--------|------|------|----------|
| POST | `/api/v1/auth/register` | — | Регистрация пациента или врача (без админа) |
| POST | `/api/v1/auth/login` | — | Логин, возвращает JWT |
| GET  | `/api/v1/auth/me` | Любой | Текущий пользователь |

`POST /auth/login`:
```json
{ "username": "admin", "password": "password" }
```
Ответ:
```json
{
  "accessToken": "eyJ…",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "fullName": "Администратор Системы",
  "roles": ["ROLE_ADMIN"]
}
```

## Users (`/api/v1/users`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/users?page=&size=` | ADMIN |
| GET | `/users/{id}` | ADMIN, DOCTOR |
| PUT | `/users/me` | любая |
| POST | `/users/me/password` | любая |
| PATCH | `/users/{id}/enabled` | ADMIN |

## Doctors (`/api/v1/doctors`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/doctors?specialization=` | любая |
| GET | `/doctors/{id}` | любая |
| GET | `/doctors/me` | DOCTOR |
| PUT | `/doctors/me/schedule` | DOCTOR |
| POST | `/doctors` | ADMIN |
| PUT | `/doctors/{id}` | ADMIN |
| DELETE | `/doctors/{id}` | ADMIN |

## Patients (`/api/v1/patients`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/patients` | ADMIN, DOCTOR |
| GET | `/patients/{id}` | ADMIN, DOCTOR |
| GET | `/patients/me` | PATIENT |
| PUT | `/patients/me` | PATIENT |
| POST | `/patients` | ADMIN |
| PUT | `/patients/{id}` | ADMIN |

## Appointments (`/api/v1/appointments`)

| Method | Path | Роль |
|--------|------|------|
| POST | `/appointments` | PATIENT |
| GET | `/appointments` | ADMIN |
| GET | `/appointments/me` | PATIENT, DOCTOR |
| GET | `/appointments/me/upcoming` | PATIENT, DOCTOR |
| GET | `/appointments/{id}` | любая авторизованная |
| PATCH | `/appointments/{id}/status` | владелец / DOCTOR / ADMIN |

`POST /appointments`:
```json
{ "doctorId": 1, "appointmentDate": "2026-05-10T10:00:00", "notes": "Плановый осмотр" }
```

## Symptoms (`/api/v1/symptoms`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/symptoms?q=` | любая |
| POST | `/symptoms` | ADMIN |
| PUT | `/symptoms/{id}` | ADMIN |
| DELETE | `/symptoms/{id}` | ADMIN |

## Diagnoses (`/api/v1/diagnoses`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/diagnoses` | любая |
| GET | `/diagnoses/{id}` | любая |
| POST | `/diagnoses` | ADMIN |
| PUT | `/diagnoses/{id}` | ADMIN |
| DELETE | `/diagnoses/{id}` | ADMIN |

## Recommendations (`/api/v1/recommendations`)

| Method | Path | Роль |
|--------|------|------|
| POST | `/recommendations/analyze` | PATIENT |
| GET | `/recommendations/me` | PATIENT |

`POST /recommendations/analyze`:
```json
{ "symptomIds": [2, 3, 7] }
```
Ответ:
```json
{
  "id": 1,
  "patientId": 1,
  "symptomIds": [2,3,7],
  "diagnosisId": 1,
  "diagnosisName": "ОРВИ",
  "recommendedDoctorId": 1,
  "recommendedDoctorName": "Иванов Иван Иванович",
  "recommendedDoctorSpecialization": "Терапевт",
  "confidence": 100,
  "createdAt": "2026-04-26T21:01:58"
}
```

## Medical Records (`/api/v1/medical-records`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/medical-records/me` | PATIENT |
| GET | `/medical-records/by-doctor` | DOCTOR |
| GET | `/medical-records/patient/{id}` | DOCTOR, ADMIN |
| POST | `/medical-records` | DOCTOR |

## Admin (`/api/v1/admin`)

| Method | Path | Роль |
|--------|------|------|
| GET | `/admin/stats/overview` | ADMIN |

## Формат ошибок

```json
{
  "timestamp": "2026-04-26T21:00:25.963Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Неверный логин или пароль",
  "path": "/api/v1/auth/login"
}
```
