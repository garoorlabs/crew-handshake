# Frontend structure

The frontend lives in `/frontend` and follows a feature-first layout:

```
src/app
  core/
  shared/ui/
  features/<feature>/
```

HTTP calls are allowed only in `features/*/data-access`.
