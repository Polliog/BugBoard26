# Infrastructure Progress — BugBoard26

> Ultimo aggiornamento: 2026-02-20

---

## Root

- [x] `Makefile` — target: dev, build, test
- [x] `docker-compose.dev.yml` — backend + frontend + postgres per dev locale
- [x] `.gitignore` aggiornato (web/, api/, no frontend/)

## Docker

- [ ] `api/Dockerfile` — multi-stage build Java 23
- [ ] `web/Dockerfile` — multi-stage build Node
- [x] `docker-compose.dev.yml` — servizi: api, web, postgres

## Da fare

- [ ] Creare Dockerfile per api/
- [ ] Creare Dockerfile per web/
- [ ] Testare docker-compose.dev con `docker compose -f docker-compose.dev.yml up`
- [ ] Eliminare cartella `frontend/` originale (bloccata da processo, da fare manualmente)
