# Error contract

All backend endpoints return a safe, stable error payload:

```
{ errorCode, message, fieldErrors? }
```

Allowed `errorCode` values:

- `UNAUTHORIZED`
- `FORBIDDEN`
- `NOT_FOUND`
- `VALIDATION_ERROR`
- `RATE_LIMITED`
- `CONFLICT`
- `UNKNOWN`

Frontend FeatureApi services map these into UI-safe categories.
