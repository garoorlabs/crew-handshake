# Known issues / follow-ups

## CSRF token mismatch on active company selection

Issue: The SPA sometimes fails CSRF validation when calling `POST /api/v1/me/active-company`,
which blocks company selection.

Temporary mitigation: `SecurityConfig` currently ignores CSRF for `/api/v1/me/active-company`
so users can select an active company.

Follow-up: Align CSRF cookie/header handling between the frontend and Spring Security
(and remove the CSRF exemption for `/api/v1/me/active-company`).
