# Backend Progress — BugBoard26 (`api/`)

> Ultimo aggiornamento: 2026-02-20
> Stack: Java 23 + Spring Boot 3.x + H2/PostgreSQL + JWT

---

## Infrastruttura

- [ ] Progetto Maven inizializzato (`pom.xml` con tutte le dipendenze)
- [ ] `application.yml` configurato (H2 dev, JWT, server port 8080)
- [ ] `Bugboard26Application.java` main class
- [ ] `DataSeeder` con admin default (`admin@bugboard.com` / `admin123`)

## Enums (`model/enums/`)

- [ ] `GlobalRole.java` — ADMIN, USER, EXTERNAL
- [ ] `IssueType.java` — QUESTION, BUG, DOCUMENTATION, FEATURE
- [ ] `IssuePriority.java` — BASSA, MEDIA, ALTA, CRITICA
- [ ] `IssueStatus.java` — APERTA, IN_PROGRESS, RISOLTA, CHIUSA

## Entità JPA (`model/`)

- [ ] `User.java` — id, email, passwordHash, name, role, createdAt
- [ ] `Issue.java` — id, title, type, description, priority, status, assignedTo, createdBy, image, archived, labels, history
- [ ] `Comment.java` — id, issue, user, content, createdAt, updatedAt
- [ ] `HistoryEntry.java` — id, issue, user, action, timestamp
- [ ] `Label.java` — id, name
- [ ] `Notification.java` — id, user, issue, message, read, createdAt

## DTO Request (`dto/request/`)

- [ ] `LoginRequest.java` — record con @NotBlank, @Email
- [ ] `CreateUserRequest.java` — record con @NotBlank, @Email, @Size, @NotNull
- [ ] `CreateIssueRequest.java` — record con validazione (RF02)
- [ ] `UpdateIssueRequest.java` — record PATCH semantics (RF06, RF13)
- [ ] `CreateCommentRequest.java` — record
- [ ] `UpdateCommentRequest.java` — record
- [ ] `ExportRequest.java` — record (RF08)

## DTO Response (`dto/response/`)

- [ ] `AuthResponse.java` — token + UserResponse
- [ ] `UserResponse.java` — con `from(User)` factory, mai espone passwordHash
- [ ] `IssueResponse.java` — con `from(Issue)` factory completo
- [ ] `CommentResponse.java` — con `from(Comment)` factory
- [ ] `HistoryEntryResponse.java` — con `from(HistoryEntry)` factory
- [ ] `NotificationResponse.java` — con `from(Notification)` factory
- [ ] `PagedResponse.java` — generico `<T>` con `of(Page<T>)` factory

## Repository (`repository/`)

- [ ] `UserRepository.java` — findByEmail
- [ ] `IssueRepository.java` — query filtri, search, paginazione
- [ ] `CommentRepository.java` — findByIssueId
- [ ] `NotificationRepository.java` — findByUserId, unread count

## Security (`security/`)

- [ ] `JwtService.java` — genera e valida token JWT (jjwt 0.12.x)
- [ ] `JwtFilter.java` — OncePerRequestFilter, estrae token da Authorization header
- [ ] `UserDetailsServiceImpl.java` — carica utente per Spring Security

## Config (`config/`)

- [ ] `SecurityConfig.java` — CORS, filtro JWT, regole autorizzazione per ruolo
- [ ] `JwtConfig.java` — proprietà JWT da application.yml

## Service (`service/`)

- [ ] `AuthService.java` — login, genera token (RF01)
- [ ] `UserService.java` — CRUD utenti, solo ADMIN (RF01)
- [ ] `IssueService.java` — CRUD issue, filtri, paginazione (RF02, RF03, RF06, RF11, RF13)
- [ ] `CommentService.java` — CRUD commenti (solo autore modifica, autore/admin elimina)
- [ ] `NotificationService.java` — crea notifiche, segna come lette (RF06)
- [ ] `ExportService.java` — export CSV + PDF (RF08)
- [ ] `PermissionService.java` — canCreateIssue, canModifyIssue, canChangeStatus, canArchive, canComment, canManageUsers

## Controller (`controller/`)

- [ ] `AuthController.java` — POST /auth/login, GET /auth/me
- [ ] `UserController.java` — GET/POST/DELETE /users (solo ADMIN)
- [ ] `IssueController.java` — GET/POST/PATCH /issues con filtri e paginazione
- [ ] `CommentController.java` — CRUD /issues/{issueId}/comments
- [ ] `NotificationController.java` — GET /notifications, PATCH /notifications/{id}/read
- [ ] `ExportController.java` — GET /issues/export?format=csv|pdf

## Test (`src/test/`)

- [ ] `IssueServiceTest.java` — min 3 metodi non banali (RF02, RF06, RF13)
- [ ] `PermissionServiceTest.java` — tutti i canX()
- [ ] `AuthServiceTest.java` — RF01
- [ ] `ExportServiceTest.java` — RF08
- [ ] `IssueControllerTest.java` — @WebMvcTest con MockMvc

---

## Note

- Compilatore: Java 23 con `-Xlint:all`, zero warning
- Tutti i DTO sono `record` Java
- `@Transactional` solo sui service, mai sui controller
- UUID generati da JPA: `@GeneratedValue(strategy = GenerationType.UUID)`
- Account admin default creato dal DataSeeder
