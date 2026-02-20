.PHONY: dev build test

dev:
	cd api && ./mvnw spring-boot:run &
	cd web && pnpm dev

build:
	cd api && ./mvnw package -DskipTests
	cd web && pnpm build

test:
	cd api && ./mvnw test
	cd web && pnpm test
