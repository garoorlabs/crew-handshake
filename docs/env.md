# Environment variables

Backend configuration is provided via environment variables.

Required (local defaults are in `backend/.env.example`):

- `DB_URL` (Postgres JDBC URL)
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `COOKIE_SECURE` (`false` in local dev)
- `PUBLIC_APP_BASE_URL`
- `SMS_PROVIDER` (`noop` for local dev)

Do not commit real `.env` files or secrets.
