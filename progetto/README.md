# BugBoard26

Sistema single-tenant per la gestione collaborativa di issue, sviluppato come progetto di Ingegneria del Software presso l'Universita degli Studi di Napoli Federico II.

BugBoard26 permette a un team di segnalare bug, richiedere feature, gestire lo stato delle issue e collaborare tramite commenti, il tutto con un sistema di ruoli e permessi granulare.

---

## Indice

- [Architettura](#architettura)
- [Tech Stack](#tech-stack)
- [Requisiti](#requisiti)
- [Quick Start](#quick-start)
- [Sviluppo](#sviluppo)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Struttura del Progetto](#struttura-del-progetto)
- [API Reference](#api-reference)
- [Sistema dei Ruoli](#sistema-dei-ruoli)
- [Configurazione](#configurazione)

---

## Architettura

```
                          +-------------------+
                          |      Nginx        |
                          |  (reverse proxy)  |
                          |   :80 / :443      |
                          +--------+----------+
                                   |
                    +--------------+--------------+
                    |                             |
           +-------v--------+          +---------v-------+
           |    Frontend     |          |     Backend     |
           |   SvelteKit     |          |  Spring Boot    |
           |     :5173       |          |     :8080       |
           +----------------+          +--------+--------+
                                                |
                                       +--------v--------+
                                       |   PostgreSQL    |
                                       |     :5432       |
                                       +-----------------+
```

Il sistema e composto da due applicazioni indipendenti:

- **Backend** (`api/`): API REST in Spring Boot che gestisce autenticazione, issue, commenti, notifiche ed export.
- **Frontend** (`web/`): SPA in SvelteKit che consuma le API e offre l'interfaccia utente.
- **Nginx**: reverse proxy opzionale per HTTPS locale e routing unificato in produzione.

---

## Tech Stack

### Backend
| Tecnologia | Versione | Scopo |
|---|---|---|
| Java | 23 (Amazon Corretto) | Linguaggio |
| Spring Boot | 3.4.x | Framework |
| Spring Security | 6.x | Autenticazione e autorizzazione |
| Spring Data JPA | 3.x | Persistenza |
| jjwt | 0.12.6 | Token JWT |
| PostgreSQL | 17 | Database |
| H2 | - | Database per test |
| JaCoCo | 0.8.12 | Code coverage |
| JUnit 5 + Mockito 5 | - | Testing |

### Frontend
| Tecnologia | Versione | Scopo |
|---|---|---|
| SvelteKit | 2.x | Framework |
| Svelte | 5.x | UI (runes: `$state`, `$derived`, `$effect`) |
| Tailwind CSS | 4.x | Styling |
| TypeScript | 5.x | Type safety |

### Infrastruttura
| Tecnologia | Scopo |
|---|---|
| Docker Compose | Orchestrazione container |
| Nginx | Reverse proxy, SSL termination |
| GitHub Actions | CI/CD pipeline |
| SonarCloud | Analisi qualita del codice |
| Bruno | Test API end-to-end |

---

## Requisiti

- **Docker** e **Docker Compose** (per lo sviluppo containerizzato)
- **mkcert** (opzionale, per HTTPS locale)

Per lo sviluppo senza Docker:
- Java 23 (Amazon Corretto)
- Node.js 22+ e pnpm
- PostgreSQL 17

---

## Quick Start

### 1. Clona il repository

```bash
git clone https://github.com/Polliog/BugBoard26.git
cd BugBoard26/progetto
```

### 2. Configura l'ambiente

```bash
cp env/dev.example .env
```

### 3. Avvia lo stack

```bash
make all
```

Questo comando avvia tre container:

| Servizio | URL | Descrizione |
|---|---|---|
| Frontend | http://localhost:5173 | Interfaccia web |
| Backend | http://localhost:8080 | API REST |
| Database | localhost:5432 | PostgreSQL |

### 4. Login

L'applicazione crea automaticamente un account admin al primo avvio:

| Campo | Valore |
|---|---|
| Email | `admin@bugboard.com` |
| Password | `admin123` |

---

## Sviluppo

### Comandi principali (Makefile)

```bash
# Stack completo con Docker
make all                # Avvia db + api + web
make backend            # Solo db + api
make stop               # Ferma tutto
make restart            # Riavvia tutto
make logs               # Segui i log dei container

# Sviluppo locale (senza Docker)
make dev                # Avvia api (Maven) + web (pnpm) localmente

# HTTPS locale
make https              # Avvia con Nginx + certificati mkcert
make https-down         # Ferma lo stack HTTPS

# Shell nei container
make shell-api          # Shell nel container API
make shell-web          # Shell nel container frontend
make shell-db           # psql nel container database

# Produzione
make prod-up            # Build e avvio stack produzione
make prod-down          # Ferma stack produzione
make prod-config        # Valida configurazione produzione

# Pulizia
make clean              # Ferma tutto e rimuove i volumi
```

### HTTPS locale con mkcert

Per sviluppare con HTTPS (utile per testare cookie sicuri, CORS con credenziali, ecc.):

```bash
# Installa mkcert se non presente
# macOS: brew install mkcert
# Linux: sudo apt install mkcert
# Windows: choco install mkcert

make https
```

Lo script genera automaticamente certificati self-signed e avvia Nginx come reverse proxy su https://localhost.

---

## Testing

### Backend (JUnit 5 + Mockito 5)

```bash
make backend-test          # Esegui i test
make backend-coverage      # Test + report JaCoCo
```

Il report di coverage viene generato in `api/target/site/jacoco/index.html`.

### Frontend

```bash
make frontend-check        # Type check (svelte-check) + build
```

### API End-to-End (Bruno)

Bruno esegue test HTTP reali contro il backend in esecuzione:

```bash
# Assicurati che lo stack sia avviato (make all)
make bruno-test
```

I test coprono: login, autenticazione, CRUD issue, listing utenti.

### Tutti i test

```bash
make test                  # backend-test + frontend-check
```

---

## CI/CD

Il progetto usa GitHub Actions con i seguenti workflow:

| Workflow | Trigger | Descrizione |
|---|---|---|
| `backend-test.yml` | Push su `api/` | Build + JUnit + JaCoCo coverage |
| `frontend-test.yml` | Push su `web/` | Type check + build |
| `bruno-test.yml` | Push su `api/` o `BrunoTesting/` | Test API E2E in container Docker |
| `sonar.yml` | Push/PR su `main` | Analisi SonarCloud |
| `pr-gate.yml` | Pull request su `main` | Gate: blocca merge se un test fallisce |

### PR Gate

Ogni pull request verso `main` deve superare tutti e tre i test suite (backend, frontend, Bruno) prima di poter essere mergiata.

### SonarCloud

L'analisi della qualita del codice e disponibile su SonarCloud. Il progetto e configurato per inviare i report di coverage JaCoCo.

---

## Struttura del Progetto

```
progetto/
├── api/                          # Backend Spring Boot
│   ├── src/main/java/            # Codice sorgente
│   │   └── it/unina/bugboard26/
│   │       ├── config/           # SecurityConfig, JwtConfig
│   │       ├── controller/       # REST controllers (6)
│   │       ├── service/          # Business logic (7)
│   │       ├── model/            # Entita JPA (6)
│   │       ├── repository/       # Spring Data repos
│   │       ├── dto/              # Request/Response records
│   │       ├── security/         # JWT filter e service
│   │       └── enums/            # GlobalRole, IssueType, etc.
│   ├── src/test/java/            # Test JUnit 5
│   ├── pom.xml
│   └── Dockerfile
│
├── web/                          # Frontend SvelteKit
│   ├── src/
│   │   ├── routes/               # Pagine (login, issues, dashboard)
│   │   └── lib/
│   │       ├── api/              # Client HTTP con JWT
│   │       ├── stores/           # Stato reattivo (Svelte 5 runes)
│   │       ├── components/       # Componenti UI e issue
│   │       └── utils/            # Permessi, date
│   ├── package.json
│   └── Dockerfile
│
├── nginx/                        # Configurazione reverse proxy
│   ├── dev.conf                  # Config dev (HTTPS, HMR websocket)
│   ├── prod.conf                 # Config produzione (static + proxy)
│   └── Dockerfile                # Build frontend + serve con Nginx
│
├── BrunoTesting/                 # Test API end-to-end
│   ├── BugBoard/                 # Collezione Bruno
│   │   ├── 00_Setup/             # Login e setup token
│   │   ├── API/                  # Test per Auth, Issues, Users, Comments
│   │   └── environments/         # Ambienti (ci, local)
│   └── Dockerfile
│
├── scripts/                      # Script di utilita
│   ├── dev_https_up.sh           # Setup HTTPS locale con mkcert
│   └── bootstrap_bruno_ci.sh     # Seed dati test per CI
│
├── .github/workflows/            # Pipeline CI/CD
├── env/                          # Template variabili d'ambiente
│   ├── dev.example
│   └── production.example
│
├── docker-compose.yml            # Stack dev
├── docker-compose.prod.yml       # Stack produzione
├── docker-compose.ci.yml         # Override per CI
├── sonar-project.properties      # Configurazione SonarCloud
├── Makefile                      # Comandi di build e gestione
└── CLAUDE.md                     # Specifica tecnica completa
```

---

## API Reference

Base URL: `http://localhost:8080`

### Autenticazione

| Metodo | Endpoint | Descrizione | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login, ritorna JWT | No |
| GET | `/api/auth/me` | Utente corrente | Si |
| GET | `/api/auth/health` | Health check | No |

### Utenti (solo ADMIN)

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/users` | Lista utenti |
| POST | `/api/users` | Crea utente |
| DELETE | `/api/users/{id}` | Elimina utente |

### Issue

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/issues` | Lista con filtri e paginazione |
| POST | `/api/issues` | Crea issue (ADMIN, USER) |
| GET | `/api/issues/{id}` | Dettaglio issue |
| PATCH | `/api/issues/{id}` | Aggiorna issue |

**Parametri di query per GET /api/issues:**

| Parametro | Tipo | Esempio |
|---|---|---|
| `type` | enum | `BUG,FEATURE` |
| `status` | enum | `TODO,IN_PROGRESS` |
| `priority` | enum | `ALTA,CRITICA` |
| `assignedToId` | UUID | `550e8400-...` |
| `search` | string | `login bug` |
| `archived` | boolean | `true` |
| `page` | int | `0` |
| `pageSize` | int | `20` |
| `sortBy` | string | `createdAt` |
| `order` | string | `desc` |

### Commenti

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/issues/{issueId}/comments` | Lista commenti |
| POST | `/api/issues/{issueId}/comments` | Crea commento (ADMIN, USER) |
| PATCH | `/api/issues/{issueId}/comments/{id}` | Modifica (solo autore) |
| DELETE | `/api/issues/{issueId}/comments/{id}` | Elimina (autore o ADMIN) |

### Notifiche

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/notifications` | Lista notifiche |
| PATCH | `/api/notifications/{id}/read` | Segna come letta |

### Export

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/issues/export?format=csv` | Export CSV |
| GET | `/api/issues/export?format=pdf` | Export PDF |

---

## Sistema dei Ruoli

BugBoard26 ha tre ruoli globali. Non esistono ruoli per-progetto.

| Ruolo | Crea Issue | Commenta | Cambia Stato | Archivia | Gestisce Utenti |
|---|---|---|---|---|---|
| **ADMIN** | Si | Si | Tutte le issue | Si | Si |
| **USER** | Si | Si | Solo issue assegnate | No | No |
| **EXTERNAL** | No | No | No | No | No |

L'utente EXTERNAL ha accesso in sola lettura: puo visualizzare issue e commenti ma non puo modificare nulla.

---

## Configurazione

### Variabili d'ambiente

Copia `env/dev.example` in `.env` nella root del progetto:

```bash
cp env/dev.example .env
```

| Variabile | Descrizione | Default |
|---|---|---|
| `POSTGRES_DB` | Nome database | `bugboard` |
| `POSTGRES_USER` | Utente database | `bugboard` |
| `POSTGRES_PASSWORD` | Password database | `bugboard` |
| `JWT_SECRET` | Chiave segreta per JWT (min 32 char) | dev secret |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Strategia DDL | `update` |
| `BUGBOARD_CORS_ALLOWED_ORIGINS` | Origini CORS (comma-separated) | `http://localhost:5173` |
| `VITE_API_URL` | URL backend per il frontend | `http://localhost:8080` |

### Produzione

Per un deploy in produzione:

1. Copia `env/production.example` in `.env`
2. Configura password sicure, JWT secret, dominio e certificati SSL
3. Esegui `make prod-up`

---

## Licenza

Progetto sviluppato per il corso di Ingegneria del Software -- Universita degli Studi di Napoli Federico II.
