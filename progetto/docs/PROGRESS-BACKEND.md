# Backend Progress — BugBoard26 (`api/`)

> Ultimo aggiornamento: 2026-02-20
> Stack: Java 23 + Spring Boot 3.x + H2/PostgreSQL + JWT

---

## Infrastruttura

- [x] Progetto Maven inizializzato (`pom.xml` con tutte le dipendenze)
- [x] `application.yml` configurato (H2 dev, JWT, server port 8080)
- [x] `Bugboard26Application.java` main class
- [x] `DataSeeder` con admin default (`admin@bugboard.com` / `admin123`)

## Enums (`model/enums/`)

- [x] `GlobalRole.java` — ADMIN, USER, EXTERNAL
- [x] `IssueType.java` — QUESTION, BUG, DOCUMENTATION, FEATURE
- [x] `IssuePriority.java` — BASSA, MEDIA, ALTA, CRITICA
- [x] `IssueStatus.java` — APERTA, IN_PROGRESS, RISOLTA, CHIUSA

## Entità JPA (`model/`)

- [x] `User.java` — id, email, passwordHash, name, role, createdAt
- [x] `Issue.java` — id, title, type, description, priority, status, assignedTo, createdBy, image, archived, labels, history
- [x] `Comment.java` — id, issue, user, content, createdAt, updatedAt
- [x] `HistoryEntry.java` — id, issue, user, action, timestamp
- [x] `Label.java` — id, name
- [x] `Notification.java` — id, user, issue, message, read, createdAt

## DTO Request (`dto/request/`)

- [x] `LoginRequest.java` — record con @NotBlank, @Email
- [x] `CreateUserRequest.java` — record con @NotBlank, @Email, @Size, @NotNull
- [x] `CreateIssueRequest.java` — record con validazione (RF02)
- [x] `UpdateIssueRequest.java` — record PATCH semantics (RF06, RF13)
- [x] `CreateCommentRequest.java` — record
- [x] `UpdateCommentRequest.java` — record
- [x] `ExportRequest.java` — record (RF08)

## DTO Response (`dto/response/`)

- [x] `AuthResponse.java` — token + UserResponse
- [x] `UserResponse.java` — con `from(User)` factory, mai espone passwordHash
- [x] `IssueResponse.java` — con `from(Issue)` factory completo
- [x] `CommentResponse.java` — con `from(Comment)` factory
- [x] `HistoryEntryResponse.java` — con `from(HistoryEntry)` factory
- [x] `NotificationResponse.java` — con `from(Notification)` factory
- [x] `PagedResponse.java` — generico `<T>` con `of(Page<T>)` factory

## Repository (`repository/`)

- [x] `UserRepository.java` — findByEmail, existsByEmail
- [x] `IssueRepository.java` — query filtri, search, paginazione (@Query JPQL)
- [x] `CommentRepository.java` — findByIssueId
- [x] `NotificationRepository.java` — findByUserId, unread count
- [x] `LabelRepository.java` — findByName

## Security (`security/`)

- [x] `JwtService.java` — genera e valida token JWT (jjwt 0.12.x)
- [x] `JwtFilter.java` — OncePerRequestFilter, estrae token da Authorization header
- [x] `UserDetailsServiceImpl.java` — carica utente per Spring Security

## Config (`config/`)

- [x] `SecurityConfig.java` — CORS, filtro JWT, regole autorizzazione per ruolo
- [x] `JwtConfig.java` — proprietà JWT da application.yml

## Service (`service/`)

- [x] `AuthService.java` — login, genera token (RF01)
- [x] `UserService.java` — CRUD utenti, solo ADMIN (RF01)
- [x] `IssueService.java` — CRUD issue, filtri, paginazione (RF02, RF03, RF06, RF11, RF13)
- [x] `CommentService.java` — CRUD commenti (solo autore modifica, autore/admin elimina)
- [x] `NotificationService.java` — crea notifiche, segna come lette (RF06)
- [x] `ExportService.java` — export CSV + PDF (RF08)
- [x] `PermissionService.java` — canCreateIssue, canModifyIssue, canChangeStatus, canArchive, canComment, canManageUsers

## Controller (`controller/`)

- [x] `AuthController.java` — POST /api/auth/login, GET /api/auth/me
- [x] `UserController.java` — GET/POST/DELETE /api/users (solo ADMIN)
- [x] `IssueController.java` — GET/POST/PATCH /api/issues con filtri e paginazione
- [x] `CommentController.java` — CRUD /api/issues/{issueId}/comments
- [x] `NotificationController.java` — GET /api/notifications, PATCH /api/notifications/{id}/read
- [x] `ExportController.java` — GET /api/issues/export?format=csv|pdf

## Test (`src/test/`)

- [x] `IssueServiceTest.java` — 6 test (RF02, RF06, RF13)
- [x] `PermissionServiceTest.java` — 14 test per tutti i canX()
- [x] `AuthServiceTest.java` — 5 test (RF01)
- [x] `ExportServiceTest.java` — 3 test (RF08)
- [x] `IssueControllerTest.java` — 4 test @WebMvcTest con MockMvc

---

## Da verificare

- [ ] Compilazione con `./mvnw compile` senza warning
- [ ] Test passano con `./mvnw test`
- [ ] DataSeeder crea admin correttamente
- [ ] CORS funzionante con frontend su :5173
