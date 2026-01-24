# ARCHITECTURE.md -- Crew Handshake (Authoritative)

This document is the architectural constitution for this repository. It is designed to prevent chaos, enforce boundaries, and ensure the implementation matches the approved PRD and `AGENTS.MD` guardrails.

All implementation decisions in this document are mandatory unless explicitly updated in a reviewed change to this file.

---

## 1) Architecture Goals

### Goals (what this architecture optimizes for)
1. Simplicity over cleverness
   One backend app, REST only, straightforward layering, minimal state patterns in the UI.

2. Hard boundaries between frontend and backend
   Angular and Spring Boot live in separate folders, deploy independently, and communicate only via `/api/v1`.

3. Hard boundaries inside the frontend
   UI components never call HTTP directly. All HTTP is done through FeatureApi in data-access.

4. Hard boundaries inside the backend
   Controllers are thin; business logic is in services; repositories are persistence-only; DTOs are used for API responses.

5. Multi-tenant safety
   Tenant isolation is enforced server-side for every authenticated request and every token-scoped worker request.

6. Field reliability under partial adoption
   The system works even if many workers do not respond or do not check in/out (roll call fallback and exception workflows).

7. Testability of core flows
   Critical flows (crew call, handshake, time capture, exceptions, payroll export) are integration tested.

### What this architecture does not optimize for (intentional)
- Complex project management workflows (Gantt, tasks, job costing)
- Offline-first native experiences
- Multi-service or microservice scaling
- Realtime event streaming or websockets
- Highly customized per-customer business logic

---

## 2) System Overview (High Level)

### Frontend responsibilities (Angular v20+)
- Provide user interfaces for three experiences:
  1) Worker public pages (token-based, no login)
  2) Foreman portal (OTP login, crew-scoped)
  3) Admin portal (OTP login, company-wide)
- Enforce accessibility (WCAG AA) and token-based design system usage
- Perform HTTP only via FeatureApi services (data-access)
- Normalize errors and show safe user-facing messages
- Never implement business logic that belongs in backend services (authorization, dispatch rules, payroll rules)

### Backend responsibilities (Spring Boot REST)
- Own the API contract under `/api/v1`
- Enforce authentication, authorization, and tenant isolation
- Implement all business rules:
  - Dispatch authority
  - Crew call generation and recipient token creation
  - Handshake states and allowed actions
  - Idempotent check-in/out
  - Roll call updates
  - Exceptions and time adjustments with audit trail
  - Payroll period calculation and CSV export
- Integrate outbound SMS provider via an interface abstraction
- Provide safe errors with stable error codes

### Communication
- Frontend calls backend via HTTPS using relative paths only:
  - `/api/v1/...`
- REST only. No GraphQL. No Supabase SDK. No BFF server.

### Deployment
- Split deploy:
  - Frontend: static assets served at `/`
  - Backend: API served at `/api` (reverse proxy path routing recommended)
- Same-origin routing is the default target:
  - `https://app.example.com/` -> frontend
  - `https://app.example.com/api/v1/...` -> backend
- Local development mirrors production routing using an Angular proxy.

---

## 3) Repository Structure (Authoritative)

### Root structure (non-negotiable)
```
/project-root
AGENTS.MD
/frontend     # Angular only (TypeScript)
/backend      # Spring Boot only (Java)
```

---

### 3.1 Frontend structure (`/frontend`)

Authoritative app structure:
```
/frontend/src/app
core/
auth/            # session handling, guards, OTP login flows, company selection
http/            # interceptors, global error mapping, request IDs
config/          # runtime config loader (if needed), environment adapters
ui/              # application shells/layouts (admin/foreman/worker)
shared/
ui/              # reusable primitives (Button, Card, Input, Modal, etc.)
util/            # pure TS utilities (no Angular deps)
directives/
pipes/
features/ <feature>/
routes/        # lazy route definitions for the feature
pages/         # container components (wires up data + state)
components/    # presentational components (no HTTP)
data-access/   # FeatureApi services (the only place allowed to use HttpClient)
domain/        # pure types/validators (optional, no Angular required)
state/         # optional store/facade (only when required by rules below)
```

#### Ownership rules
- `core/`
  - Owns app-wide concerns: auth/session, guards, interceptors, shells.
  - Forbidden: feature-specific business logic, feature-specific UI.

- `shared/ui/`
  - Owns reusable primitives and design system implementations.
  - Forbidden: HTTP, feature-specific data logic, feature-specific API types.

- `features/<feature>/pages/`
  - Owns orchestration and state wiring (signals, derived state).
  - Forbidden: `HttpClient` import/calls, direct endpoint strings.

- `features/<feature>/components/`
  - Owns rendering and user interactions via inputs/outputs.
  - Forbidden: HTTP, session logic, routing logic beyond local UI concerns.

- `features/<feature>/data-access/`
  - Owns all HTTP calls for the feature.
  - Allowed: `HttpClient`, endpoint paths, DTO mapping, error normalization.
  - Forbidden: UI code, DOM interactions.

#### Global forbidden frontend behaviors
- No HTTP calls in pages/components outside `data-access`
- No absolute API URLs; only `/api/v1/...`
- No cross-feature imports of internals (only public exports)
- No ad-hoc styling outside token-based design system

---

### 3.2 Backend structure (`/backend`)

Authoritative structure (feature-first, layered per feature):
```
/backend/src/main/java/<base>/
config/                 # Spring configuration, security config, CORS (if needed)
common/
errors/               # error DTO, error codes, exception mapping
security/             # auth/session helpers, OTP utilities
tenant/               # active company context helpers
time/                 # clock abstraction if needed (optional)
features/
auth/
api/                # controllers + request/response DTOs
service/            # auth logic, OTP flow
persistence/        # entities + repositories (if needed)
admin/
api/
service/
persistence/
foreman/
api/
service/
persistence/
worker/
api/
service/
persistence/
messaging/
service/            # SmsProvider interface + implementations + templates
payroll/
api/
service/
persistence/
```

#### Ownership rules
- `api/` (controllers + DTOs)
  - Controllers: request mapping, validation (`@Valid`), status codes.
  - Forbidden: business logic, database access, authorization decisions beyond calling service checks.

- `service/`
  - Owns business logic and authorization enforcement.
  - Owns transaction boundaries (`@Transactional`) where needed.
  - Forbidden: direct HTTP concerns (no `HttpServletResponse` manipulation beyond exceptions).

- `persistence/`
  - Owns JPA entities and repositories.
  - Forbidden: business logic, permission checks, cross-tenant findById without company scope.

#### DTO boundaries
- JPA entities must never be returned directly from controllers.
- API request/response DTOs must be Java `record` types where reasonable.
- Mapping from entity -> response DTO occurs in service layer (or a mapper called by service).

#### Transaction rules
- Transactions are defined at service layer only.
- Controllers are non-transactional by default.
- Repositories do not open transactions.

---

## 4) Frontend Architecture (Angular v20+)

### 4.1 Page vs component responsibilities
- Page components (in `pages/`)
  - Fetch data via FeatureApi
  - Own page-level signals (`loading`, `error`, `data`)
  - Own routing param extraction
  - Compute derived state with `computed()`
  - Assemble presentational components

- Presentational components (in `components/`)
  - Render UI based on inputs
  - Emit outputs (events)
  - No API knowledge, no HTTP, no global state

### 4.2 FeatureApi contract (mandatory)
Each feature's FeatureApi:
- Is the only place to call `HttpClient`
- Owns endpoint path strings (relative, `/api/v1/...`)
- Maps backend DTOs -> UI models
- Normalizes errors into frontend error categories (Section 10)
- Exposes methods returning Observables or Promises (team standard), with conversion to signals in pages

### 4.3 Store/Facade usage rules (strict)
A Store/Facade under `features/<feature>/state/` is allowed only when:
- multiple pages share state, or
- a page is multi-step with complex transitions, or
- caching/retry/polling is necessary, or
- page component is becoming a god component

Otherwise, do not add a store. Keep state local to the page via signals.

### 4.4 State management strategy
- Use signals for mutable UI state
- Use `computed()` for derived state (e.g., worker primary CTA state)
- Avoid global state libraries
- Keep transformations pure and predictable

### 4.5 Routing and lazy loading
- All feature routes are lazy-loaded.
- Worker routes (`/w/...`), foreman routes (`/f/...`), admin routes (`/a/...`) are separated at top-level.
- Routes enforce auth guards:
  - Worker public routes: no auth guard; token resolution flow
  - Foreman/Admin routes: OTP/session guard + active company guard

### 4.6 Error handling flow (frontend)
- FeatureApi maps backend errors (`errorCode`) into a small set:
  - Unauthorized, Forbidden, NotFound, Validation, RateLimited, Unknown
- Pages render standardized components:
  - Worker: token expired/invalid -> dedicated friendly screen
  - Foreman/Admin: error banner + retry
- UI never renders raw backend error bodies.

### 4.7 Forbidden UI behaviors (non-negotiable)
- UI must not:
  - call `HttpClient`
  - build endpoint URLs outside FeatureApi
  - interpret raw `HttpErrorResponse` in pages/components
  - implement authorization logic (hide UI based on capabilities, but backend enforces)

---

## 5) Backend Architecture (Spring Boot)

### 5.1 Controllers (mandatory behavior)
Controllers:
- Validate inputs (`@Valid`)
- Parse request params and path variables
- Call service methods
- Map responses to DTOs
- Return correct HTTP status codes

Controllers must never contain:
- business rules (dispatch authority, allowed actions, payroll rules)
- repository calls
- tenant scoping logic beyond passing context to service
- complex transformations

### 5.2 Services (source of truth for business logic)
Services:
- Enforce:
  - dispatch authority
  - crew scoping for foremen
  - token scoping for workers
  - time entry idempotency
  - exception generation and resolution rules
  - audit logging
- Own transaction boundaries (`@Transactional`)
- Call repositories and messaging provider
- Return DTOs or domain objects mapped to DTOs

### 5.3 Repositories (persistence only)
Repositories:
- Provide persistence operations scoped by `companyId`
- Must not contain business rules
- Must not expose unscoped `findById` methods used in services without company filtering

### 5.4 Validation flow
- Request DTOs use `jakarta.validation` annotations.
- Service-layer validation covers business rules (e.g., check-out without check-in).

### 5.5 Error handling via `@ControllerAdvice`
- Centralized exception mapping
- Safe response shape:
  - `{ errorCode, message, fieldErrors? }`
- No stack traces or internal exception messages returned to clients.

---

## 6) Frontend <-> Backend Contract

### Ownership of API contracts
- Backend owns the REST contract under `/api/v1`.
- Frontend consumes it via FeatureApi services.

### Versioning strategy
- API base path is versioned (`/api/v1`).
- Changes to `/api/v1` must be backward compatible within the MVP lifecycle.

### Breaking changes handling
Breaking changes are disallowed without:
1) Updating the PRD/contract docs
2) Coordinated change to both backend DTOs and frontend FeatureApi mapping
3) Adding migration support (if data shape changes)

### DTO evolution rules
- Additive changes are allowed (adding fields).
- Renaming or removing fields is not allowed without coordinated updates.
- Responses must remain stable in meaning; do not overload fields.

---

## 7) Data Flow (Critical Paths)

This section maps directly to PRD user journeys. For each flow: request -> validation -> service -> persistence -> response, including failure paths.

### 7.1 OTP login (Foreman/Admin)
1) Frontend calls `POST /api/v1/auth/otp/start` with phone
2) Backend validates phone format -> service generates OTP -> sends SMS
3) Frontend calls `POST /api/v1/auth/otp/verify` with code
4) Backend validates -> establishes session cookie
Failure paths:
- invalid code -> UNAUTHORIZED
- too many attempts -> RATE_LIMITED

### 7.2 Active company selection
1) Frontend calls `POST /api/v1/me/active-company`
2) Backend verifies membership exists -> sets activeCompanyId in session
Failure paths:
- company not in memberships -> FORBIDDEN

### 7.3 Admin roster setup (workers/crews/sites)
1) Frontend calls `/api/v1/admin/*` endpoints via FeatureApi
2) Backend validates DTOs -> service enforces company scope -> repositories persist records
Failure paths:
- validation errors -> VALIDATION_ERROR
- unauthorized session -> UNAUTHORIZED
- missing permissions -> FORBIDDEN

### 7.4 Create and send Crew Call (dispatch)
1) Frontend calls `POST /api/v1/foreman/crew-calls` or admin equivalent
2) Backend:
   - controller validates request
   - service enforces dispatch authority and crew scope
   - persists CrewCall + CrewCallRecipients
   - generates per-recipient tokens (store token hashes)
   - sends outbound SMS per recipient
3) Response returns crewCallId + send summary
Failure paths:
- dispatch not allowed -> FORBIDDEN
- SMS provider error -> returns success with per-recipient failure details; also logs event

### 7.5 Worker token resolution and Crew Call view
1) Worker opens `/w/t/:token` -> frontend calls `GET /api/v1/public/worker/links/{token}`
2) Backend resolves token hash -> returns destination type/path
Failure paths:
- token invalid/expired -> NOT_FOUND

3) Frontend loads `GET /api/v1/public/worker/crew-calls/by-link/{token}`
4) Backend returns crew call details + allowed actions + current state
Failure paths:
- token invalid/expired -> NOT_FOUND
- crew call cancelled -> CONFLICT

### 7.6 Worker handshake and availability
1) Worker posts handshake -> backend validates allowed action -> updates recipient state
2) If Cant/Need change, worker posts availability -> backend records and returns closure state
Failure paths:
- invalid action for state -> CONFLICT or VALIDATION_ERROR (service-defined)
- token expired -> NOT_FOUND

### 7.7 Worker check-in/out (idempotent)
1) Worker posts check-in -> service finds/creates time entry unique to worker+date+crewCall -> sets checkInAt if empty
2) Worker posts check-out -> service sets checkOutAt if empty
Failure paths:
- check-out before check-in -> VALIDATION_ERROR
- outside allowed window -> VALIDATION_ERROR

### 7.8 Foreman roll call fallback
1) Foreman posts roll call -> service validates crew scope -> creates/updates time entries for PRESENT
Failure paths:
- worker not in crew roster -> VALIDATION_ERROR
- foreman not assigned to crew -> FORBIDDEN

### 7.9 Exceptions resolution and time adjustment
1) Foreman/Admin requests exceptions list -> service derives/loads OPEN exceptions
2) Resolve exception:
   - approve or adjust time with reason
   - service writes audit log
Failure paths:
- invalid resolution -> VALIDATION_ERROR
- foreman tries cross-crew resolution -> FORBIDDEN

### 7.10 Payroll period summary and CSV export
1) Admin requests payroll period summary -> service computes boundaries from company settings -> aggregates time entries and exception counts
2) Admin exports CSV -> service streams CSV response
Failure paths:
- settings invalid -> VALIDATION_ERROR
- none (export allowed even if unresolved exceptions exist; rows include status flags)

---

## 8) Security and Access Control

### Authentication responsibility
- Backend authenticates Foreman/Admin via OTP and session cookie.
- Workers use token-scoped public endpoints only.

### Authorization checks (where enforced)
- Authorization is enforced in service layer for every authenticated request:
  - role checks (ADMIN vs FOREMAN)
  - crew scope checks for foremen
  - dispatch authority checks for crew call creation
- Worker endpoints authorize via token resolution:
  - token -> recipient -> company context

### Role validation flow
- Controllers pass session identity context to services.
- Services validate:
  - membership exists
  - activeCompanyId set
  - role allowed
  - scope allowed

### Forbidden shortcuts
- No authorization checks in frontend as the source of truth.
- No repository queries without company scoping.
- No public endpoints that accept a companyId from the client.

---

## 9) Configuration and Environments

### Backend environment variables (required)
- Database:
  - `DB_URL`
  - `DB_USER`
  - `DB_PASSWORD`
- Auth/session:
  - `JWT_SECRET` (if JWT cookie)
  - `COOKIE_SECURE` (true in staging/prod)
- Public URL for link generation:
  - `PUBLIC_APP_BASE_URL`
- SMS provider:
  - `SMS_PROVIDER` (`noop` for local dev)
  - provider-specific keys (if not noop)

### Frontend runtime config rules
- Frontend uses relative API paths only.
- No environment-based API host switching in UI code.
- Any runtime config is limited to non-secret values (e.g., app name display), loaded via `core/config`.

### Local dev parity with production
- Frontend dev server proxies `/api` to backend.
- Backend serves under `/api/v1`.
- No CORS-based development setup is used as the primary path.

---

## 10) Error Handling Strategy

### Backend error normalization
- All errors return a stable shape:
  - `errorCode` (stable string)
  - `message` (safe, user-friendly)
  - `fieldErrors` (optional)
- Error codes are stable and limited:
  - `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `VALIDATION_ERROR`, `RATE_LIMITED`, `CONFLICT`, `UNKNOWN`

### Frontend error categories
FeatureApi maps backend errors into:
- Unauthorized
- Forbidden
- NotFound
- Validation
- RateLimited
- Unknown

### UI behavior per error type
- Unauthorized:
  - Foreman/Admin: redirect to login
- Forbidden:
  - show not permitted state; hide action links
- NotFound:
  - Worker: Link expired or invalid. Ask your foreman to resend.
- Validation:
  - show inline field errors; prevent submission until fixed
- RateLimited:
  - show cooldown messaging (OTP/resend)
- Unknown:
  - safe generic error; show retry

---

## 11) Testing Strategy (Architecture-Level)

### Unit tests
- Backend:
  - token hashing/validation
  - dispatch authority checks
  - time entry idempotency rules
  - payroll period boundary calculations
- Frontend:
  - worker primary CTA state machine logic
  - error normalization mapping

### Integration tests
- Backend (required):
  - crew call send -> worker fetch by token
  - handshake transitions
  - check-in/out idempotency
  - roll call creates time entries
  - exception resolution creates audit events
  - payroll CSV export
  - tenant isolation attempts (must fail)

### End-to-end tests (minimum smoke)
- Worker flow: open link -> confirm -> check in -> check out
- Foreman flow: view today -> roll call -> resolve exception
- Admin flow: roster setup -> payroll export

### What is not tested at this layer
- Provider-specific SMS delivery reliability (covered by provider integration testing and monitoring)
- Pixel-perfect UI rendering (covered by design reviews and accessibility checks)

---

## 12) Anti-Patterns (Explicitly Forbidden)

The following are rejected in code review:

### Frontend anti-patterns
- Calling `HttpClient` outside `features/**/data-access`
- Building endpoint URLs in pages/components
- Using absolute API hosts or environment-specific URL switching in UI
- Duplicating backend business rules in UI (authorization, allowed actions)
- Ad-hoc styles outside design tokens
- Non-lazy-loaded feature routes

### Backend anti-patterns
- Business logic inside controllers
- Returning JPA entities in API responses
- Repository methods that load by ID without company scoping
- Using `ddl-auto=update` as a migration strategy
- Logging secrets, tokens, or OTP codes
- Inconsistent error responses or leaking stack traces
- Any non-REST API approach (GraphQL, websockets for core flows)

### Cross-cutting anti-patterns
- Introducing microservices, queues, or event buses to prepare for scale
- Adding task assignment or job costing into the MVP domain
- Adding surveillance features (continuous GPS, productivity scoring)
