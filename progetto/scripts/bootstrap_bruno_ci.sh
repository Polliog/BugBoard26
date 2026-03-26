#!/bin/sh
set -eu

# Attende che il backend sia healthy e seed dei dati per i test Bruno.
# Il DataSeeder di Spring Boot crea automaticamente l'admin (admin@bugboard.com / admin123).
# Questo script crea dati aggiuntivi per i test E2E.

API_URL="${API_URL:-http://localhost:8080}"

echo "==> Waiting for backend health..."
for i in $(seq 1 60); do
    if curl -fsS "$API_URL/api/auth/health" > /dev/null 2>&1; then
        echo "==> Backend is healthy"
        break
    fi
    if [ "$i" -eq 60 ]; then
        echo "ERROR: Backend did not become healthy in 120s"
        exit 1
    fi
    sleep 2
done

echo "==> Logging in as admin..."
TOKEN=$(curl -fsS "$API_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@bugboard.com","password":"admin123"}' \
    | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
    echo "ERROR: Login failed"
    exit 1
fi
echo "==> Admin login OK"

echo "==> Creating test user..."
curl -fsS "$API_URL/api/users" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"email":"test@bugboard.com","password":"test123456","name":"Test User","role":"USER"}' \
    > /dev/null 2>&1 || echo "    (user may already exist)"

echo "==> Creating test issue..."
ISSUE_RESPONSE=$(curl -fsS "$API_URL/api/issues" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"title":"Bruno CI Test Issue","type":"BUG","description":"Issue creata automaticamente per i test Bruno CI end-to-end.","priority":"MEDIA"}' \
    2>/dev/null || echo "")

if [ -n "$ISSUE_RESPONSE" ]; then
    ISSUE_ID=$(echo "$ISSUE_RESPONSE" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
    echo "==> Issue created: $ISSUE_ID"

    if [ -n "$ISSUE_ID" ]; then
        echo "==> Creating test comment..."
        curl -fsS "$API_URL/api/issues/$ISSUE_ID/comments" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d '{"content":"Commento di test per Bruno CI."}' \
            > /dev/null 2>&1 || true
    fi
fi

echo "==> Bruno CI bootstrap complete"
