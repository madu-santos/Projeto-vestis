package com.clothingrental.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.clothingrental.auth.config.DevAdminAccountInitializer;
import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import com.clothingrental.auth.service.StaffAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração da autenticação: sobem a aplicação completa (filtros do
 * Spring Security, controllers, templates e PostgreSQL) e simulam requisições HTTP.
 *
 * <p>{@code @Transactional} desfaz as contas criadas em cada teste, preservando o banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    private static final String ACTIVE_USERNAME = "test.admin";
    private static final String DISABLED_USERNAME = "test.disabled";
    private static final String PASSWORD = "test-only-password-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffAccountService staffAccountService;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void createAccounts() {
        staffAccountService.createAccount(ACTIVE_USERNAME, PASSWORD);
        StaffAccount disabledAccount = staffAccountService.createAccount(DISABLED_USERNAME, PASSWORD);
        disabledAccount.disable();
        staffAccountRepository.flush();
    }

    // --- Rotas públicas e protegidas ---

    @Test
    void loginPageIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void loginPageShowsGenericErrorAndLogoutMessages() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(content().string(containsString("Usuário ou senha inválidos.")));
        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(content().string(containsString("Você saiu do sistema.")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/any/page"})
    void adminAreaRedirectsVisitorToLogin(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void pathsNotExplicitlyPublicRequireAuthentication() throws Exception {
        mockMvc.perform(get("/some-private-page"))
                .andExpect(redirectedUrl("/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/catalog/any", "/garments/1", "/rental-requests/new", "/css/app.css"})
    void publicPathsDoNotRequireLogin(String path) throws Exception {
        // Ainda não existem páginas nessas rotas: 404 prova que a segurança deixou passar.
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/catalog/new", "/garments/1", "/rental-requests/delete"})
    void publicPathsRejectNonGetRequestsWithoutAuthentication(String path) throws Exception {
        mockMvc.perform(post(path).with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void userWithoutAdminRoleIsForbiddenInAdminArea() throws Exception {
        mockMvc.perform(get("/admin").with(user("visitor").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    // --- Login ---

    @Test
    void loginWithValidCredentialsRedirectsToAdmin() throws Exception {
        mockMvc.perform(formLogin().user(ACTIVE_USERNAME).password(PASSWORD))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername(ACTIVE_USERNAME).withRoles("ADMIN"));
    }

    @Test
    void loginWithCaseInsensitiveUsernameRedirectsToAdmin() throws Exception {
        mockMvc.perform(formLogin().user("  TEST.ADMIN  ").password(PASSWORD))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername(ACTIVE_USERNAME).withRoles("ADMIN"));
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(formLogin().user(ACTIVE_USERNAME).password("wrong-password-123"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void loginWithUnknownUserIsRejected() throws Exception {
        mockMvc.perform(formLogin().user("nobody").password(PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void disabledAccountCannotLogIn() throws Exception {
        mockMvc.perform(formLogin().user(DISABLED_USERNAME).password(PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void loginWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/login").param("username", ACTIVE_USERNAME).param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedSessionGivesAccessToAdminArea() throws Exception {
        MockHttpSession session = logIn();

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/home"))
                .andExpect(content().string(containsString("Acesso administrativo realizado")));
    }

    // --- Logout ---

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = logIn();

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoutWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/logout").with(user(ACTIVE_USERNAME).roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    // --- Armazenamento da senha e isolamento do perfil dev ---

    @Test
    void passwordIsStoredAsBcryptHash() {
        String storedValue = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM staff_accounts WHERE username = ?", String.class, ACTIVE_USERNAME);

        assertThat(storedValue)
                .isNotEqualTo(PASSWORD)
                .doesNotContain(PASSWORD)
                .matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
        assertThat(passwordEncoder.matches(PASSWORD, storedValue)).isTrue();
    }

    @Test
    void devAdminInitializerIsNotActiveInTestProfile() {
        assertThat(applicationContext.getBeansOfType(DevAdminAccountInitializer.class)).isEmpty();
    }

    private MockHttpSession logIn() throws Exception {
        return (MockHttpSession) mockMvc.perform(formLogin().user(ACTIVE_USERNAME).password(PASSWORD))
                .andExpect(authenticated())
                .andReturn()
                .getRequest()
                .getSession(false);
    }
}
