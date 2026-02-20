# Frontend Progress — BugBoard26 (`web/`)

> Ultimo aggiornamento: 2026-02-20
> Stack: SvelteKit (SPA, adapter-static) + Svelte 5 runes + Tailwind CSS v4

---

## Infrastruttura

- [x] Copiato `frontend/` → `web/`
- [x] `package.json` — nome "web", adapter-static
- [x] `svelte.config.js` — adapter-static con fallback
- [x] `vite.config.ts` — Tailwind + SvelteKit
- [x] `.env` — `VITE_API_URL=http://localhost:8080`
- [x] `app.css` — Tailwind imports

## Tipi (`lib/types/`)

- [x] Rimosso `Project`, `ProjectMember`, `ProjectMemberRole`
- [x] `GlobalRole`: `'ADMIN' | 'USER' | 'EXTERNAL'` (uppercase)
- [x] `User` — id, email, name, role (no password)
- [x] `Issue` — senza projectId, con User objects per assignedTo/createdBy, labels, history
- [x] `Comment` — con `author: User`
- [x] `Notification` — nuovo tipo
- [x] `PagedResponse<T>` — data, total, page, pageSize
- [x] `HistoryEntry` — id, performedBy, action, timestamp
- [x] `AuthResponse` — token + user

## API Client (`lib/api/`)

- [x] `client.ts` — fetch wrapper con Authorization Bearer, gestione errori, getRaw per export
- [x] `auth.api.ts` — login, getMe
- [x] `issues.api.ts` — getAll (filtri+paginazione), getById, create, update, exportFile
- [x] `comments.api.ts` — getByIssue, create, update, delete
- [x] `notifications.api.ts` — getAll, markAsRead
- [x] `users.api.ts` — getAll, create, delete (solo ADMIN)

## Store (`lib/stores/`)

- [x] `auth.svelte.ts` — JWT reale, `restore()`, `login()`, `logout()`, `isAdmin`, `isExternal`, `isLoggedIn`
- [x] `ui.svelte.ts` — loading state

## Utils (`lib/utils/`)

- [x] `permissions.ts` — `can(user, action, issue?)` per create:issue, comment, change:status, archive, manage:users
- [x] `dates.ts` — `formatDate()` e `formatDateTime()` con `Intl.DateTimeFormat`

## Componenti UI (`lib/components/ui/`)

- [x] `Badge.svelte` — colori allineati a CLAUDE.md, enum uppercase, labels display
- [x] `Modal.svelte` — con click-outside per chiudere
- [x] `Spinner.svelte` — tre size (sm, md, lg)

## Componenti Issue (`lib/components/issues/`)

- [x] `IssueCard.svelte` — card con Badge, formatDate, immagine
- [x] `IssueFilters.svelte` — filtri tipo/priorità/stato/assegnato/search/archiviate/sort
- [x] `IssueForm.svelte` — modale creazione con validazione e upload immagine
- [x] `CommentSection.svelte` — lista + edit inline + form nuovo commento

## Route (`routes/`)

- [x] `+layout.svelte` — auth guard globale, Toaster, restore sessione, loading spinner
- [x] `+page.svelte` — redirect a `/issues`
- [x] `login/+page.svelte` — API reale, redirect a `/issues`, validazione, Spinner
- [x] `issues/+page.svelte` — lista con filtri, paginazione, export CSV/PDF, creazione (RF03, RF08, RF11, RF13)
- [x] `issues/[issueId]/+page.svelte` — dettaglio, commenti, cambio stato, archivia (RF02, RF06)

## Rotte vecchie (eliminate da web/)

- [x] `routes/projects/` — rimossa intera cartella

## File vecchi (eliminati da web/)

- [x] `lib/services/` — rimossa intera cartella (projects.ts, mockData.ts, auth.ts, issues.ts, comments.ts)
- [x] `lib/components/Badge.svelte` — sostituito da ui/Badge.svelte
- [x] `lib/components/Modal.svelte` — sostituito da ui/Modal.svelte

## Componente aggiornato

- [x] `Navbar.svelte` — link a /issues, ruoli uppercase con label display

## Design System (corretto)

- [x] Badge Priorità: bg-X-100 text-X-800
- [x] Badge Tipo: bg-X-100 text-X-800
- [x] Badge Stato: Chiusa = bg-gray-300 text-gray-800

## Funzionalità implementate (struttura)

- [x] RF01 — Login con JWT reale (pagina + API)
- [x] RF02 — Creazione issue (modale IssueForm con validazione)
- [x] RF03 — Lista issue con filtri e paginazione
- [x] RF06 — Cambio stato issue (bottoni sulla pagina dettaglio)
- [x] RF08 — Export CSV/PDF (bottoni download)
- [x] RF11 — Ricerca per keyword (input nel filtro)
- [x] RF13 — Archiviazione (bottone solo per ADMIN via can())
- [x] RF15 — Modalità readonly per EXTERNAL (can() blocca UI)

## UI/UX

- [x] Loading states (Spinner)
- [x] Toast notifiche (svelte-sonner)
- [x] Navbar aggiornata
- [ ] Responsive mobile-first (da verificare/raffinare)
- [ ] Accessibilità completa (focus trap modali, aria-describedby)
- [ ] Gestione utenti ADMIN (pagina dedicata /admin o /users)
- [ ] Notifiche dropdown nella navbar

---

## Da fare

- [ ] `pnpm install` e verifica che compili
- [ ] Eliminare cartella `frontend/` originale
- [ ] Pagina gestione utenti (ADMIN)
- [ ] Dropdown notifiche in Navbar
- [ ] Pagina 404
- [ ] Test accessibilità
- [ ] Responsiveness check
