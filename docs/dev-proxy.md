# Dev proxy

The frontend dev server proxies `/api` to the backend so local routing matches production:

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`

The proxy config lives in `frontend/proxy.conf.json`.
