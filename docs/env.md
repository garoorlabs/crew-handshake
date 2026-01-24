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
- `OTP_RATE_LIMIT_PHONE_MAX` (default `5`)
- `OTP_RATE_LIMIT_PHONE_WINDOW` (default `PT15M`)
- `OTP_RATE_LIMIT_IP_MAX` (default `20`)
- `OTP_RATE_LIMIT_IP_WINDOW` (default `PT15M`)
- `OTP_ATTEMPT_RETENTION` (default `PT24H`)

Do not commit real `.env` files or secrets.
