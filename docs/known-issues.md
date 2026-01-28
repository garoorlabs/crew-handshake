# Known issues / follow-ups

## CSRF token mismatch on active company selection

Issue: The SPA sometimes fails CSRF validation when calling `POST /api/v1/me/active-company`,
which blocks company selection.

Temporary mitigation: `SecurityConfig` currently ignores CSRF for `/api/v1/me/active-company`
so users can select an active company.

Follow-up: Align CSRF cookie/header handling between the frontend and Spring Security
(and remove the CSRF exemption for `/api/v1/me/active-company`).

## Dev auth endpoints disabled unless flags are set

Issue: Demo login/worker-link endpoints return `Dev endpoints disabled` unless the backend
is started with dev flags enabled.

Workaround (local dev only):
- Environment variables: `SMS_PROVIDER=log`, `APP_SEED_ENABLED=true`
- Or JVM args: `-Dspring-boot.run.arguments="--app.sms-provider=log --app.seed.enabled=true"`

Follow-up: Add a documented local profile or script to enable dev auth endpoints without
manual flags.
