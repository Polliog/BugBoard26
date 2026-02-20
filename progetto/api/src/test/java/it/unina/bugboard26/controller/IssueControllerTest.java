package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.response.IssueResponse;
import it.unina.bugboard26.dto.response.PagedResponse;
import it.unina.bugboard26.dto.response.UserResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.*;
import it.unina.bugboard26.security.JwtService;
import it.unina.bugboard26.security.UserDetailsServiceImpl;
import it.unina.bugboard26.service.AuthService;
import it.unina.bugboard26.service.IssueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test per IssueController con @WebMvcTest.
 * RF02, RF03 - Creazione e lista issue.
 */
@WebMvcTest(value = IssueController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("GET /api/issues restituisce lista paginata")
    @WithMockUser(roles = "USER")
    void getIssuesReturnsPaginatedList() throws Exception {
        UserResponse creator = new UserResponse("user-id", "user@test.com", "User", GlobalRole.USER, Instant.now());
        IssueResponse issueResponse = new IssueResponse(
                "issue-1", "Test Issue", IssueType.BUG, "Descrizione test",
                IssuePriority.ALTA, IssueStatus.APERTA, null, creator,
                Instant.now(), Instant.now(), null, false, null, null,
                List.of(), List.of()
        );
        PagedResponse<IssueResponse> pagedResponse = new PagedResponse<>(
                List.of(issueResponse), 1, 0, 20
        );

        when(issueService.getAll(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/issues")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Test Issue"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("GET /api/issues/{id} restituisce issue singola")
    @WithMockUser(roles = "USER")
    void getIssueByIdReturnsIssue() throws Exception {
        UserResponse creator = new UserResponse("user-id", "user@test.com", "User", GlobalRole.USER, Instant.now());
        IssueResponse issueResponse = new IssueResponse(
                "issue-1", "Bug nel login", IssueType.BUG, "Descrizione dettagliata",
                IssuePriority.CRITICA, IssueStatus.APERTA, null, creator,
                Instant.now(), Instant.now(), null, false, null, null,
                List.of(), List.of()
        );

        when(issueService.getById("issue-1")).thenReturn(issueResponse);

        mockMvc.perform(get("/api/issues/issue-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("issue-1"))
                .andExpect(jsonPath("$.title").value("Bug nel login"))
                .andExpect(jsonPath("$.type").value("BUG"));
    }

    @Test
    @DisplayName("POST /api/issues crea una nuova issue")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void createIssueReturnsCreated() throws Exception {
        User user = new User();
        user.setId("user-id");
        user.setEmail("user@test.com");
        user.setName("User");
        user.setRole(GlobalRole.USER);
        user.setCreatedAt(Instant.now());

        UserResponse creator = UserResponse.from(user);
        IssueResponse issueResponse = new IssueResponse(
                "new-issue-id", "Nuova feature", IssueType.FEATURE, "Descrizione della feature richiesta",
                IssuePriority.MEDIA, IssueStatus.APERTA, null, creator,
                Instant.now(), Instant.now(), null, false, null, null,
                List.of(), List.of()
        );

        when(authService.getUserByEmail("user@test.com")).thenReturn(user);
        when(issueService.create(any(), any())).thenReturn(issueResponse);

        String body = """
                {
                    "title": "Nuova feature",
                    "type": "FEATURE",
                    "description": "Descrizione della feature richiesta"
                }
                """;

        mockMvc.perform(post("/api/issues")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new-issue-id"))
                .andExpect(jsonPath("$.title").value("Nuova feature"));
    }

    @Test
    @DisplayName("Richiesta non autenticata restituisce 401")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isUnauthorized());
    }
}
