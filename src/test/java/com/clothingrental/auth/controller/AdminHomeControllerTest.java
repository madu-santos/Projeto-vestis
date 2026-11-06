package com.clothingrental.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminHomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;
    private StaffAccountPrincipal principal;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(new Store("Atelier Belle Époque", "atelier-belle-epoque"));
        principal = new StaffAccountPrincipal(100L, "clara.gerente", "$2a$10$hashedpasswordplaceholder123", store.getId(), true, true);
        auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @DisplayName("GET /admin autenticado deve exibir o nome da loja e o usuário no modelo e na página")
    void adminDashboardDisplaysStoreAndUserInfo() throws Exception {
        mockMvc.perform(get("/admin").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/home"))
                .andExpect(model().attribute("storeName", "Atelier Belle Époque"))
                .andExpect(model().attribute("storeSlug", "atelier-belle-epoque"))
                .andExpect(model().attribute("username", "clara.gerente"))
                .andExpect(model().attribute("activeNav", "home"))
                .andExpect(content().string(containsString("Atelier Belle Époque")))
                .andExpect(content().string(containsString("clara.gerente")))
                .andExpect(content().string(containsString("Peças cadastradas")))
                .andExpect(content().string(containsString("Solicitações pendentes")))
                .andExpect(content().string(containsString("Locações ativas")))
                .andExpect(content().string(containsString("Clientes cadastrados")))
                .andExpect(content().string(containsString("Acesso administrativo realizado")));
    }
}
