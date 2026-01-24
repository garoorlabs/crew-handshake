# Backend structure

The backend lives in `/backend` and uses Controller → Service → Repository layering.

Feature packages are grouped under:

```
src/main/java/com/crewhandshake/features
```

Controllers are thin and return DTOs only.
