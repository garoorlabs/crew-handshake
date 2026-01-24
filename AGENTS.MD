# agents.md -- Monorepo Guardrails (Angular v20+ + Spring Boot REST) -- Simple & Effective

This repository contains **two separate applications** in one monorepo and deploys them independently.

- **Frontend:** Angular v20+ (TypeScript) in `/frontend`
- **Backend:** Spring Boot REST API (Java) in `/backend`

This guide exists so humans and AI agents implement changes **consistently** without over-architecture.

---

## 1) Repository Layout (Non-negotiable)

```
/project-root
agents.md
/frontend   # Angular only (TypeScript)
/backend    # Spring Boot only (Java)
```

Hard rules:
- OK: Java/Spring Boot server code lives only in `/backend`.
- OK: Angular/TypeScript UI code lives only in `/frontend`.
- NO: Do not create backend/server code inside the frontend package.
- NO: Do not place UI code inside the backend package.

---

## 2) Deployment Model (Split Deploy -- Required)

We deploy **two independent artifacts**:

- **Frontend:** static build output (CDN / static host)
- **Backend:** Spring Boot API (container/VM)

Monorepo is about **source organization**. Split deploy is about **runtime topology**. Keep them separate.

### 2.1 Deployment contract (provider-agnostic)
This repo must remain deployable to **any** host (Vercel, DigitalOcean, AWS, etc.).
Therefore, frontend code must not depend on a specific platform feature.

**Preferred production routing (same origin):**
- `https://app.example.com/` -> frontend static
- `https://app.example.com/api/...` -> backend (reverse proxy / rewrite / ingress path rule)

This can be implemented via:
- a platform rewrite rule (Vercel/Netlify/etc.)
- Nginx/Caddy/Traefik reverse proxy
- Kubernetes ingress path routing
- load balancer path rules

**Fallback (only if same origin is not possible):**
- frontend at `https://app.example.com`
- backend at `https://api.example.com`
- backend must enable CORS explicitly and safely

### 2.2 Frontend API URL rule (required)
Frontend must call API using **relative paths**:

- OK: `/api/v1/...`
- NO: `http://localhost:8080/...`
- NO: `https://api.example.com/...`

This keeps the same frontend build working across dev/stage/prod.

---

# FRONTEND -- `/frontend` (Angular v20+)

## 3) The One Frontend Rule That Prevents Chaos

### UI does not do HTTP
Pages/components must NOT:
- import/call `HttpClient`
- build URLs or embed endpoint paths
- handle auth headers manually
- interpret raw `HttpErrorResponse`

All HTTP calls happen in:
`/frontend/src/app/features/<feature>/data-access/*`

Default flow:
`Page -> FeatureApi -> Spring Boot REST`

> Optional: add a Store/Facade ONLY when the page becomes complex.

---

## 4) Frontend Structure (Feature-first, minimal)

```
/frontend/src/app
core/
auth/                 # auth/session, guards, login flows
http/                 # interceptors, global error mapping
config/               # environment/runtime config
ui/                   # app shell layout (header/nav/etc.)
shared/
ui/                   # reusable UI components
util/                 # pure TS helpers (no Angular deps)
directives/
pipes/
features/<feature>/
routes/               # lazy routes
pages/                # container/page components
components/           # presentational components
data-access/          # FeatureApi (only place using HttpClient)
state/                # OPTIONAL: store/facade (signals)
domain/               # OPTIONAL: pure models/rules/validators
```

Rules:
- `shared/` is UI + pure utilities only (no feature business logic).
- `core/` is cross-cutting app-wide concerns.
- Avoid cross-feature imports of internals (only use public exports if needed).

---

## 5) FeatureApi (Data Access) -- REQUIRED

Each feature owns a small, explicit API wrapper:
- `features/users/data-access/users.api.ts` -> `UsersApi`

FeatureApi responsibilities:
- call backend endpoints using `HttpClient`
- use typed DTOs
- map DTO -> UI-friendly models (when helpful)
- normalize errors into app-friendly categories (see Error Handling)
- build URLs using the base prefix **`/api/v1`** (relative)

FeatureApi must NOT:
- store UI state (belongs in optional store/facade or the page)
- contain UI behavior (toasts, navigation, DOM)

---

## 6) Optional Store/Facade (Only when needed)

A Store/Facade is a UI-focused service that exposes:
- **state** (usually signals): `data`, `loading`, `error`
- **actions**: `load()`, `save()`, `delete()`

Add it ONLY if:
- multiple components share state, or
- multi-step workflows exist (load -> transform -> save), or
- the page is becoming a "god component", or
- you need caching/retry/polling/debouncing.

If a feature is simple, skip it and call FeatureApi from the page.

Naming is flexible:
- `UsersStore`, `UsersFacade`, `UsersViewModel` are all acceptable.

---

## 7) Local Development Must Mirror Production (Proxy Required)

In local dev, frontend still calls `/api/...` and the dev server proxies to backend.

- Angular dev server proxies `/api` -> `http://localhost:8080`

This avoids CORS and prevents environment-specific URL code.

---

## 8) Frontend Error Handling Standard

FeatureApi must normalize errors into a small set:
- `Unauthorized`
- `Forbidden`
- `NotFound`
- `Validation`
- `RateLimited`
- `Unknown`

UI should:
- show safe user-friendly messages
- allow retry where appropriate
- never render raw backend internals

---

## 9) Angular + TypeScript Guardrails (MUST FOLLOW)

### TypeScript Best Practices
- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid `any`; use `unknown` when uncertain and narrow safely

### Angular Best Practices
- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators (Angular v20+ default)
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use `@HostBinding` and `@HostListener`
  Put host bindings inside the `host` object of `@Component` / `@Directive` instead
- Use `NgOptimizedImage` for all static images
  (`NgOptimizedImage` does not work for inline base64 images)

### Accessibility Requirements
- MUST pass AXE checks
- MUST meet WCAG AA minimums (focus, contrast, ARIA, keyboard navigation)

### Components
- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use `computed()` for derived state
- Set `changeDetection: ChangeDetectionStrategy.OnPush`
- Prefer inline templates for small components
- Prefer Reactive Forms over Template-driven ones
- Do NOT use `ngClass` (use class bindings instead)
- Do NOT use `ngStyle` (use style bindings instead)
- When using external templates/styles, use paths relative to the component TS file.

### State Management
- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals -- use `update` or `set` instead

### Templates
- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Use the async pipe to handle observables
- Do not assume globals like `Date` are available
- Do not write arrow functions in templates (they are not supported)

### Services
- Single responsibility
- Use `providedIn: 'root'` for true singletons (or provide at feature route if scoped)
- Use the `inject()` function instead of constructor injection

---

# BACKEND -- `/backend` (Spring Boot REST)

## 10) Backend Architecture (Simple, modern)

Use the classic layering:
`Controller -> Service -> Repository`

Hard rules:
- Controllers are thin (HTTP + validation + status codes only).
- Business logic lives in services.
- Repositories are persistence only.
- NO: Never expose JPA entities directly in API responses.
- OK: Use request/response DTOs (prefer Java `record`).

Recommended package layout (feature-first):
```
/backend/src/main/java/<base-package>/
config/
common/
features/<feature>/
api/             # controllers + request/response DTOs
service/         # business logic + transactions
persistence/     # entities + repositories
```

---

## 11) Backend Guardrails (Modern defaults)

- Spring Boot 3.x
- Java 17+ (21 preferred if available)
- Constructor injection only (no field injection)

### Validation
- Use `jakarta.validation` annotations on request DTOs
- Controllers accept `@Valid` inputs

### Error handling (safe by default)
- Centralize errors via `@ControllerAdvice`
- Never return stack traces or internal exception messages
- Return a stable `errorCode` for client behavior
- Validation errors should include field-level details

### Security
- Secure-by-default (explicit public endpoints)
- Prefer same-origin routing via `/api` proxy (minimizes CORS complexity)
- If CORS is required, configure explicit allowed origins (no `*` with credentials)
- Never log secrets/tokens

### Database & transactions
- Migrations via Flyway/Liquibase recommended
- No `ddl-auto=update` in production
- Use `@Transactional` at the service layer

---

## 12) Backend API Conventions

- Base path: `/api/v1`
- Resource naming: nouns (`/users`, `/orders`)
- Correct HTTP status codes:
  - 200/201/204 success
  - 400/401/403/404/409/422/429 client errors (as appropriate)
  - 500 unexpected errors (safe response only)
- Prefer additive response changes; avoid breaking shapes.

---

# FRONTEND <-> BACKEND INTEGRATION

## 13) Contract rules

- Backend owns the contract under `/api/v1`.
- Frontend consumes it via FeatureApi services.
- Coordinate breaking changes; prefer additive evolution.

Optional (when the app grows):
- Add `/contracts/openapi.yaml` as a shared reference.

---

# CI/CD (Keep monorepo fast)

## 14) Path-based pipelines (Required)

- Frontend CI/CD runs only when `/frontend/**` changes
- Backend CI/CD runs only when `/backend/**` changes
- Optional: `/contracts/**` triggers both

Each pipeline:
- runs lint + tests + build
- produces its own deployable artifact

---

# 15) "Where does this code go?" (Quick Guide)

- UI rendering & interaction -> `/frontend` presentational component
- Screen composition / routing / params -> `/frontend` page component
- Shared state + workflows -> `/frontend` optional store/facade
- HTTP calls + mapping -> `/frontend` FeatureApi (required)
- Endpoints / auth / business logic / DB access -> `/backend`

---

# 16) Definition of Done (For Humans + AI Agents)

Any change must:
- respect folder boundaries (`/frontend` vs `/backend`)
- keep frontend HTTP inside FeatureApi only
- keep backend logic out of controllers and use DTOs
- maintain accessibility on UI changes (AXE + WCAG AA)
- include tests when behavior changes (where practical)
- keep templates simple and avoid complex logic
