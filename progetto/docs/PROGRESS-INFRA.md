# Infrastructure Progress — BugBoard26

> Ultimo aggiornamento: 2026-02-20

---

## Root

- [ ] `Makefile` — target: dev, build, test
- [ ] `docker-compose.dev.yml` — backend + frontend + postgres per dev locale
- [ ] `.gitignore` aggiornato

## Docker

- [ ] `api/Dockerfile` — multi-stage build Java 23
- [ ] `web/Dockerfile` — multi-stage build Node
- [ ] `docker-compose.dev.yml` — servizi: api, web, postgres

## Docker Compose Dev

```yaml
# Target:
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: bugboard
      POSTGRES_USER: bugboard
      POSTGRES_PASSWORD: bugboard
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  api:
    build: ./api
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bugboard
      SPRING_DATASOURCE_USERNAME: bugboard
      SPRING_DATASOURCE_PASSWORD: bugboard
      SPRING_JPA_HIBERNATE_DDL_AUTO: create-drop
      JWT_SECRET: dev-secret-256-bit-change-in-production
    depends_on:
      - postgres

  web:
    build: ./web
    ports:
      - "5173:5173"
    environment:
      VITE_API_URL: http://localhost:8080
    depends_on:
      - api

volumes:
  pgdata:
```

---

## Note
- In dev, si può anche usare H2 in-memory senza Docker per il backend
- docker-compose.dev è per chi vuole l'ambiente completo con PostgreSQL
