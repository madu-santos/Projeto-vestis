package com.clothingrental.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.clothingrental.auth.config.DevAdminAccountInitializer;
import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import com.clothingrental.auth.service.StaffAccountService;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.repository.StoreRepository;
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
 * Testes de integração da autenticação e segurança multi-tenant: sobem a aplicação completa
 * (filtros do Spring Security, controllers, templates e PostgreSQL) e simulam requisições HTTP.
 *
 * <p>{@code @Transactional} desfaz os registros criados em cada teste, preservando o banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    private static final String STORE_SLUG = "loja-teste";
    private static final String ACTIVE_USERNAME = "test.admin";
    private static final String DISABLED_USERNAME = "test.disabled";
    private static final String PASSWORD = "test-only-password-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

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

    private Store sampleStore;

    @BeforeEach
    void setUpTestData() {
        sampleStore = storeRepository.save(new Store("Loja de Teste", STORE_SLUG));
        staffAccountService.createAccount(ACTIVE_USERNAME, PASSWORD, sampleStore);
        StaffAccount disabledAccount = staffAccountService.createAccount(DISABLED_USERNAME, PASSWORD, sampleStore);
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

    @ParameterizedTest
    @ValueSource(strings = {"/some-private-page", "/catalog/any", "/garments/1", "/rental-requests/new"})
    void pathsNotExplicitlyPublicRequireAuthentication(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/lojas/loja-teste",
            "/lojas/aurora",
            "/lojas/festa-e-cia/vestidos",
            "/css/app.css",
            "/js/app.js",
            "/images/logo.png"
    })
    void publicPathsDoNotRequireLogin(String path) throws Exception {
        // Rotas públicas liberadas pelo Spring Security: 404 comprova que a segurança permitiu o acesso direto
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicStorePostSolicitacoesWithCsrfAllowedWithoutLogin() throws Exception {
        // Rota POST /lojas/*/solicitacoes liberada publicamente para clientes solicitarem interesse
        // Status 404 confirma que passou pelas regras de autorização do Spring Security
        mockMvc.perform(post("/lojas/loja-teste/solicitacoes").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicStorePostSolicitacoesWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/lojas/loja-teste/solicitacoes"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/lojas/aurora", "/lojas/aurora/config", "/lojas/qualquer-loja"})
    void storeNonGetRoutesAreExplicitlyDeniedEvenForAuthenticatedAdmin(String path) throws Exception {
        // Administrador autenticado tentando executar métodos não permitidos em rotas públicas de vitrine
        mockMvc.perform(put(path).with(user(ACTIVE_USERNAME).roles("ADMIN")).with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch(path).with(user(ACTIVE_USERNAME).roles("ADMIN")).with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete(path).with(user(ACTIVE_USERNAME).roles("ADMIN")).with(csrf()))
                .andExpect(status().isForbidden());

        // Qualquer outro POST em /lojas/** que não seja /lojas/*/solicitacoes deve ser negado
        mockMvc.perform(post(path).with(user(ACTIVE_USERNAME).roles("ADMIN")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/lojas/aurora", "/lojas/aurora/config"})
    void storeNonGetRoutesRejectAnonymousVisitors(String path) throws Exception {
        mockMvc.perform(put(path).with(csrf()))
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(patch(path).with(csrf()))
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(delete(path).with(csrf()))
                .andExpect(redirectedUrl("/login"));
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
    void staffFromDisabledStoreCannotLogIn() throws Exception {
        sampleStore.disable();
        storeRepository.save(sampleStore);
        storeRepository.flush();

        mockMvc.perform(formLogin().user(ACTIVE_USERNAME).password(PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void reactivatingStoreAllowsActiveStaffToLoginWhileDisabledStaffRemainsBlocked() throws Exception {
        sampleStore.disable();
        storeRepository.save(sampleStore);
        storeRepository.flush();

        // 1. Loja desativada: nem ativo nem inativo conseguem logar
        mockMvc.perform(formLogin().user(ACTIVE_USERNAME).password(PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());

        mockMvc.perform(formLogin().user(DISABLED_USERNAME).password(PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());

        // 2. Loja reativada: funcionário ativo consegue logar, mas o desativado permanece bloqueado
        sampleStore.enable();
        storeRepository.save(sampleStore);
        storeRepository.flush();

        mockMvc.perform(formLogin().user(ACTIVE_USERNAME).password(PASSWORD))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername(ACTIVE_USERNAME).withRoles("ADMIN"));

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

    @Test
    void activeSessionIsDeniedImmediatelyWhenStoreIsDisabledDuringTheSession() throws Exception {
        // 1. Funcionária de uma loja ativa realiza login com sucesso
        MockHttpSession session = logIn();

        // 2. Sistema permite o acesso ao painel administrativo /admin
        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/home"));

        // 3. A loja é desativada diretamente no banco durante a mesma sessão
        sampleStore.disable();
        storeRepository.save(sampleStore);
        storeRepository.flush();

        // 4. A funcionária tenta acessar novamente /admin utilizando a mesma sessão anterior
        // 5. O sistema nega o acesso (HTTP 403 Forbidden) em tempo real
        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isForbidden());

        // 6. Ao reativar a loja, o acesso da funcionária volta a ser permitido
        sampleStore.enable();
        storeRepository.save(sampleStore);
        storeRepository.flush();

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk());
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
