# Crew Handshake

Monorepo for the Crew Handshake MVP.

## Repository structure

```
/
  frontend/   Angular v20+
  backend/    Spring Boot REST
```

Authoritative references:
- `PRD.md`
- `ARCHITECTURE.md`
- `DESIGN_SYSTEM.md`
- `AGENTS.MD`

## Prerequisites

- Node.js 20+
- Java 21+ (17 minimum)
- Docker (for local Postgres)

## Quick start

1) Start Postgres:

```
docker compose up -d
```

2) Start backend (from repo root):

```
scripts/backend-dev.sh
```

3) Start frontend (from repo root):

```
scripts/frontend-dev.sh
```

The frontend dev server proxies `/api` to the backend so local routing matches production.

## Scripts

- `scripts/backend-dev.sh`
- `scripts/frontend-dev.sh`
- `scripts/lint.sh`
- `scripts/test.sh`
- `scripts/build.sh`
- `scripts/verify.sh`

## Environment

See `docs/env.md` and `backend/.env.example`.

## Docs

- `docs/env.md`
- `docs/ci.md`
- `docs/error-contract.md`
- `docs/design-system-usage.md`
- `docs/frontend-structure.md`
- `docs/backend-structure.md`
- `docs/dev-proxy.md`
