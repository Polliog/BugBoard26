SHELL := /bin/sh

COMPOSE := docker compose
COMPOSE_PROXY := docker compose --profile proxy
COMPOSE_CI := docker compose -f docker-compose.yml -f docker-compose.ci.yml
PROD_API_IMAGE ?= bugboard26/api:local-prod
PROD_WEB_IMAGE ?= bugboard26/web:local-prod

.PHONY: dev build test all stop logs restart \
        https https-down \
        backend frontend \
        backend-test backend-coverage frontend-check \
        bruno-test \
        shell-api shell-web shell-db \
        prod-up prod-down prod-config \
        clean

# ── Development ──────────────────────────────────────────────

# Start the full dev stack (db + api + web)
all:
	$(COMPOSE) up --build

# Start just the backend service (+ database)
backend:
	$(COMPOSE) up --build db api

# Start just the frontend service (+ backend + db)
frontend:
	$(COMPOSE) up --build

# Start without Docker (local Java + pnpm)
dev:
	cd api && ./mvnw spring-boot:run &
	cd web && pnpm dev

# Start full stack with HTTPS reverse proxy (nginx on 80/443)
https:
	./scripts/dev_https_up.sh

# Stop stack started with HTTPS profile
https-down:
	$(COMPOSE_PROXY) down

# Bring the entire stack down
stop:
	$(COMPOSE) down

# Restart all services
restart:
	$(COMPOSE) down
	$(COMPOSE) up --build

# Follow container logs
logs:
	$(COMPOSE) logs --tail=50 --follow

# ── Building ─────────────────────────────────────────────────

# Build both api and web Docker images
build:
	$(COMPOSE) build

# ── Testing ──────────────────────────────────────────────────

# Run all tests (backend + frontend check)
test: backend-test frontend-check

# Run backend JUnit tests
backend-test:
	cd api && ./mvnw test -B

# Run backend tests with JaCoCo coverage report
backend-coverage:
	cd api && ./mvnw verify -B
	@echo "Coverage report: api/target/site/jacoco/index.html"

# Run frontend type check + build
frontend-check:
	cd web && pnpm check && pnpm build

# Run Bruno API E2E tests (requires stack running)
bruno-test:
	$(COMPOSE_CI) run --rm bruno run \
		00_Setup/Login.bru \
		API/Auth/Me.bru \
		API/Issues/CreateIssue.bru \
		API/Issues/ListIssues.bru \
		API/Users/ListUsers.bru \
		--env ci

# ── Shells ───────────────────────────────────────────────────

# Open a shell in the api container
shell-api:
	$(COMPOSE) exec api sh

# Open a shell in the web container
shell-web:
	$(COMPOSE) exec web sh

# Open a shell in the database container
shell-db:
	$(COMPOSE) exec db psql -U $${POSTGRES_USER:-bugboard} -d $${POSTGRES_DB:-bugboard}

# ── Production ───────────────────────────────────────────────

# Build and start production-like stack
prod-up:
	docker build -t $(PROD_API_IMAGE) ./api
	docker build -t $(PROD_WEB_IMAGE) -f nginx/Dockerfile .
	API_IMAGE=$(PROD_API_IMAGE) WEB_IMAGE=$(PROD_WEB_IMAGE) \
		$(COMPOSE) -f docker-compose.prod.yml up -d

# Stop production-like stack
prod-down:
	API_IMAGE=$(PROD_API_IMAGE) WEB_IMAGE=$(PROD_WEB_IMAGE) \
		$(COMPOSE) -f docker-compose.prod.yml down

# Validate production compose definition
prod-config:
	API_IMAGE=example/api:test WEB_IMAGE=example/web:test \
		$(COMPOSE) --env-file env/production.example -f docker-compose.prod.yml config > /dev/null

# ── Cleanup ──────────────────────────────────────────────────

# Stop stack and remove volumes
clean:
	$(COMPOSE) down -v
	$(COMPOSE_CI) down -v 2>/dev/null || true
