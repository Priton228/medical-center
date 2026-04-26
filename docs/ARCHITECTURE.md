# Архитектура системы

## Общий вид

Проект представляет собой распределённую систему из двух независимых деплоимых компонентов:

```
┌──────────────┐     HTTP/JSON      ┌──────────────────┐    JDBC    ┌────────────────┐
│  React SPA   │  ───────────────▶  │  Spring Boot 3   │  ───────▶  │ PostgreSQL 15  │
│  (Vite/TS)   │  ◀───── JWT ────── │  REST API + JWT  │            │  (Flyway DDL)  │
└──────────────┘                    └──────────────────┘            └────────────────┘
       :5173 (dev) / :80 (nginx)                :8080                      :5432
```

* **Frontend** — одностраничное приложение (SPA) на React 18 + TypeScript, собирается Vite, обслуживается Nginx в production. Связь с бэкендом — REST/JSON, авторизация — `Authorization: Bearer <jwt>`.
* **Backend** — монолитный Spring Boot, разделён на слои: `controller → service → repository → domain`. Безопасность — Spring Security + JWT. Миграции схемы — Flyway. ORM — Spring Data JPA / Hibernate.
* **PostgreSQL** — главное и единственное хранилище. Все связи между сущностями — внешние ключи. БД нормализована до 3НФ.

## Слои бэкенда

| Слой | Назначение | Примеры |
|------|------------|---------|
| `controller` | HTTP API, валидация, маршруты, `@PreAuthorize` | `AppointmentController`, `RecommendationController` |
| `service` | Бизнес-логика, транзакции (`@Transactional`) | `AppointmentService`, `RecommendationService` |
| `repository` | Spring Data JPA, кастомные `@Query` | `AppointmentRepository`, `DiagnosisRepository` |
| `domain` | JPA-сущности и enum'ы | `User`, `Appointment`, `Symptom` |
| `dto` | Java records для DTO-границы | `AuthDtos.JwtResponse`, `RecommendationDtos.AnalyzeRequest` |
| `mapper` | Перенос Entity ↔ DTO | `Mappers` |
| `security` | JWT, фильтр, `UserDetailsService`, конфиг | `JwtService`, `SecurityConfig` |
| `exception` | Кастомные исключения, `@RestControllerAdvice` | `NotFoundException`, `GlobalExceptionHandler` |

## Аутентификация и авторизация

1. Клиент отправляет `POST /api/v1/auth/login` с логином/паролем.
2. `AuthService` валидирует через `AuthenticationManager` (BCrypt сравнение).
3. Возвращается JWT (HS256, 60 минут) с claims: `sub=username`, `uid`, `roles`.
4. Каждый защищённый запрос передаёт `Authorization: Bearer ...`.
5. `JwtAuthFilter` парсит токен, поднимает `UserDetails`, кладёт в `SecurityContext`.
6. `@PreAuthorize("hasRole('ADMIN')")` и подобные ограничения применяются к методам контроллеров.

## Модуль рекомендаций (Strategy + Facade)

```
PatientSymptoms.tsx  ──POST── /api/v1/recommendations/analyze
                          │
                          ▼
              RecommendationController
                          │
                          ▼
              RecommendationService (Facade)
                          │
        ┌─────────────────┼──────────────────┐
        ▼                 ▼                  ▼
DiagnosisRepository   SymptomMatchingStrategy   DoctorRepository
                       (Weighted impl.)
```

* `SymptomMatchingStrategy` — интерфейс (паттерн **Strategy**), позволяющий заменить алгоритм на ML/правила без изменений в фасаде.
* `WeightedSymptomMatchingStrategy` — реализация по умолчанию: считает долю совпавших симптомов и нормализует уверенность.
* Найденный диагноз → ищется первый доступный врач соответствующей специализации.
* Каждый запрос сохраняется в `recommendations` как история анализов.

## Использованные паттерны

| Паттерн | Где |
|---------|-----|
| Repository | Spring Data JPA репозитории |
| Strategy | `SymptomMatchingStrategy` |
| Facade | `RecommendationService` объединяет 4 репозитория и стратегию |
| DTO | Java records в `dto/` |
| Builder | Lombok `@Builder` на сущностях |
| Singleton (через DI) | все Spring beans |
| Front Controller | `DispatcherServlet` |

## Frontend

* **Routing** (`react-router-dom`) — иерархия с `RoleGuard`, который делает `<Navigate />` на основании ролей пользователя.
* **State**: Redux Toolkit (`authSlice` с persist в `localStorage`).
* **Server cache**: TanStack Query — кэш и инвалидация при мутациях.
* **HTTP**: axios + interceptor, добавляющий JWT и редиректящий на login при 401.
* **UI**: Tailwind CSS + кастомные классы (`.card`, `.btn-primary`); анимации Framer Motion на ключевых переходах.
* **3 layouts** (`PatientLayout`, `DoctorLayout`, `AdminLayout`) — единый `SidebarLayout` с разным набором ссылок.

## Деплой

`docker-compose.yml` запускает весь стек:

* `postgres` — PostgreSQL 15 с healthcheck `pg_isready`.
* `backend` — multi-stage Maven сборка → JRE 17 рантайм. Зависит от `postgres`.
* `frontend` — Node 20 build → Nginx alpine. Проксирует `/api/` на `backend:8080`.

Переменные окружения (`SPRING_DATASOURCE_*`, `MEDCENTER_JWT_SECRET`, `VITE_API_BASE_URL`) задаются в compose.
