# Frontend Progress — BugBoard26 (`web/`)

> Ultimo aggiornamento: 2026-02-20
> Stack: SvelteKit (SPA, adapter-static) + Svelte 5 runes + Tailwind CSS v4

---

## Infrastruttura

- [ ] Rinominare `frontend/` → `web/`
- [ ] `package.json` — nome "web", adapter-static
- [ ] `svelte.config.js` — adapter-static
- [ ] `vite.config.ts` — Tailwind + SvelteKit
- [ ] `.env` — `VITE_API_URL=http://localhost:8080`
- [ ] `app.css` — Tailwind imports

## Tipi (`lib/types/`)

- [ ] Rimuovere `Project`, `ProjectMember`, `ProjectMemberRole`
- [ ] `UserRole` → `GlobalRole`: `'ADMIN' | 'USER' | 'EXTERNAL'` (uppercase)
- [ ] `User` — id, email, name, role (no password)
- [ ] `Issue` — rimuovere `projectId`, aggiungere `assignedTo: UserResponse | null`, `createdBy: UserResponse`, `labels`, `history`
- [ ] `Comment` — aggiornare con `author: UserResponse`
- [ ] `Notification` — nuovo tipo
- [ ] `PagedResponse<T>` — data, total, page, pageSize
- [ ] `HistoryEntry` — id, performedBy, action, timestamp

## API Client (`lib/api/`)

- [ ] `client.ts` — fetch wrapper con Authorization Bearer, gestione errori
- [ ] `auth.api.ts` — login, getMe
- [ ] `issues.api.ts` — getAll (filtri+paginazione), getById, create, update, export
- [ ] `comments.api.ts` — getByIssue, create, update, delete
- [ ] `notifications.api.ts` — getAll, markAsRead
- [ ] `users.api.ts` — getAll, create, delete (solo ADMIN)

## Store (`lib/stores/`)

- [ ] `auth.svelte.ts` — riscrivere con JWT reale, `restore()`, `login()`, `logout()`, `isAdmin`, `isExternal`
- [ ] `ui.svelte.ts` — toast state, modal state

## Utils (`lib/utils/`)

- [ ] `permissions.ts` — `can(user, action, issue?)` con azioni: create:issue, comment, change:status, archive, manage:users
- [ ] `dates.ts` — formattazione date con `Intl.DateTimeFormat`

## Componenti UI (`lib/components/ui/`)

- [ ] `Badge.svelte` — fix colori priorità (bg-X-100 text-X-800), rimuovere variante 'role'
- [ ] `Modal.svelte` — mantenere, aggiungere focus trap
- [ ] `Toast.svelte` — (opzionale, svelte-sonner già presente)
- [ ] `Spinner.svelte` — componente loading

## Componenti Issue (`lib/components/issues/`)

- [ ] `IssueCard.svelte` — card singola issue (estratto da pagina lista)
- [ ] `IssueFilters.svelte` — barra filtri (tipo, priorità, stato, assegnato, search, archiviate)
- [ ] `IssueForm.svelte` — form creazione/modifica issue (modale)
- [ ] `CommentSection.svelte` — lista commenti + form nuovo commento

## Route (`routes/`)

- [ ] `+layout.svelte` — auth guard globale, Toaster, restore sessione
- [ ] `+page.svelte` — redirect a `/issues` (non più `/projects`)
- [ ] `login/+page.svelte` — aggiornare per usare API reale, redirect a `/issues`
- [ ] `issues/+page.svelte` — lista globale issue con filtri, ricerca, export, toggle archiviate (RF03, RF08, RF11, RF13)
- [ ] `issues/[issueId]/+page.svelte` — dettaglio issue, commenti, cambio stato (RF02, RF06)
- [ ] Rimuovere `projects/` e tutte le sub-routes

## Rotte da eliminare

- [ ] `routes/projects/+page.svelte`
- [ ] `routes/projects/[id]/issues/+page.svelte`
- [ ] `routes/projects/[id]/issues/[issueId]/+page.svelte`

## File da eliminare

- [ ] `lib/services/projects.ts`
- [ ] `lib/services/mockData.ts`
- [ ] `lib/services/auth.ts` (sostituito da api/auth.api.ts)
- [ ] `lib/services/issues.ts` (sostituito da api/issues.api.ts)
- [ ] `lib/services/comments.ts` (sostituito da api/comments.api.ts)

## Design System

### Badge Priorità (da fixare)
| Valore  | Attuale (SBAGLIATO)         | Corretto (CLAUDE.md)          |
|---------|-----------------------------|-------------------------------|
| Bassa   | `bg-blue-500 text-white`    | `bg-blue-100 text-blue-800`   |
| Media   | `bg-yellow-500 text-white`  | `bg-yellow-100 text-yellow-800`|
| Alta    | `bg-orange-500 text-white`  | `bg-orange-100 text-orange-800`|
| Critica | `bg-red-500 text-white`     | `bg-red-100 text-red-800`     |

### Badge Tipo ✅ (già corretto)
### Badge Stato (leggera differenza)
| Chiusa  | `bg-gray-800 text-white`    | `bg-gray-300 text-gray-800`   |

## Funzionalità da implementare

- [ ] RF01 — Login con JWT reale + gestione utenti ADMIN
- [ ] RF02 — Creazione issue (modale IssueForm)
- [ ] RF03 — Lista issue con filtri e paginazione
- [ ] RF06 — Cambio stato issue + notifica
- [ ] RF08 — Export CSV/PDF (bottone download)
- [ ] RF11 — Ricerca per keyword
- [ ] RF13 — Archiviazione (solo ADMIN)
- [ ] RF15 — Modalità readonly per EXTERNAL (can() blocca UI)

## UI/UX

- [ ] Responsive mobile-first
- [ ] Loading states (Spinner)
- [ ] Toast notifiche
- [ ] Accessibilità (aria-label, focus trap modali)
- [ ] Navbar aggiornata (link a /issues, notifiche)

---

## Note

- Svelte 5: solo runes ($state, $derived, $effect, $props). Vietato $: reattivo
- Componenti: PascalCase
- Store/service: camelCase con suffisso (.svelte.ts, .api.ts)
- Enum frontend allineati a Java: ADMIN, USER, EXTERNAL (maiuscolo)
- Date: ISO string dal JSON, Intl.DateTimeFormat per display
