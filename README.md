# Medical Center — Система записи на приём с модулем рекомендаций по симптомам

Курсовой проект по дисциплине «Распределённые информационные системы».
Распределённое веб-приложение для автоматизации процесса записи пациентов на
приём к врачам, ведения медицинских карт и предварительной рекомендации
специалиста на основе анализа введённых пациентом симптомов.

Проект полностью разделён на два независимых модуля:

- `backend/` — REST API на **Java 17 + Spring Boot 3 + Spring Security + Spring Data JPA + PostgreSQL 15 + Flyway**;
- `frontend/` — SPA на **React 18 + TypeScript + Vite + Redux Toolkit + TanStack Query + Tailwind CSS + Framer Motion**.

Дополнительная документация:

- [docs/USE_CASES.md](docs/USE_CASES.md) — варианты использования по трём ролям (пациент, врач, администратор).
- [docs/DB_SCHEMA.md](docs/DB_SCHEMA.md) — описание схемы базы данных (11 таблиц, 3НФ).
- [docs/API.md](docs/API.md) — спецификация REST API.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — архитектура и используемые шаблоны.

## Роли пользователей

В системе ровно **три роли**, у каждой — свой раздел интерфейса:

1. `PATIENT` (пациент) — записывается на приём, вводит симптомы, получает рекомендации,
   ведёт свой профиль.
2. `DOCTOR` (врач) — управляет личным расписанием, ведёт приём, заполняет
   медицинские карты, оформляет диагноз и лечение.
3. `ADMIN` (администратор) — управляет пользователями, врачами, симптомами,
   диагнозами, формирует отчёты и просматривает статистику.

## Быстрый старт

### Вариант 1. Через Docker Compose (рекомендуется)

```bash
docker compose up --build
```

После запуска:

- Frontend — http://localhost:5173
- Backend — http://localhost:8080 (Swagger UI: http://localhost:8080/swagger-ui.html)
- PostgreSQL — `localhost:5432`, db `medical_center`, user `postgres`, password `root`

### Вариант 2. Запуск вручную

#### 1. PostgreSQL

```bash
docker run -d --name medical-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root \
  -e POSTGRES_DB=medical_center \
  -p 5432:5432 postgres:15-alpine
```

#### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Backend поднимется на `http://localhost:8080`. При первом запуске Flyway
автоматически создаст 11 таблиц и заполнит их демо-данными
(`backend/src/main/resources/db/migration`).

#### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend поднимется на `http://localhost:5173` и будет проксировать
запросы `/api/*` на backend.

## Демо-учётные записи

Все пароли — `password`.

| Роль          | Логин          |
| ------------- | -------------- |
| Администратор | `admin`        |
| Врач          | `doctor.ivanov`|
| Врач          | `doctor.petrova`|
| Пациент       | `patient.sidorov` |
| Пациент       | `patient.kozlova` |

## Соответствие требованиям задания

- Spring Boot 3+, Spring Security, Spring Data JPA, JWT, BCrypt — раздел 3.1.2.
- React 18 + TypeScript + Vite + Redux Toolkit + Tailwind CSS — раздел 3.1.3.
- PostgreSQL 15 + Flyway, ≥ 8 связанных таблиц в 3НФ — раздел 3.1.4.
- ≥ 15 высокоуровневых вариантов использования — раздел 3.2.1, [docs/USE_CASES.md](docs/USE_CASES.md).
- RESTful API с пагинацией/фильтрацией, JSON, корректные HTTP-коды — раздел 3.2.2.
- JWT + RBAC через `@PreAuthorize`, BCrypt — раздел 3.3.2.
- ООП Java 17: generics, abstract classes, records (DTO), Optional, custom
  exceptions, Stream API — раздел 3.1.1.
- Шаблоны проектирования: Strategy (`SymptomMatchingStrategy`), Factory
  (`AppointmentFactory`), Facade (`RecommendationFacade`), Builder (Lombok
  `@Builder`) — раздел 3.1.1.
