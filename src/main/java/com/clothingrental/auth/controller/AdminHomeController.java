package com.clothingrental.auth.controller;

import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.tenant.repository.StoreRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelo painel administrativo da loja (GET /admin).
 *
 * <p>Identifica o funcionário autenticado e a loja correspondente para alimentar
 * o layout com os dados reais do tenant (nome do estabelecimento, slug para vitrine
 * e identificador do usuário).
 */
@Controller
public class AdminHomeController {

    private final StoreRepository storeRepository;

    public AdminHomeController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping("/admin")
    public String home(Authentication authentication, Model model) {
        String username = "Administrador";
        String storeName = "Loja Principal";
        String storeSlug = null;

        if (authentication != null) {
            username = authentication.getName();
            if (authentication.getPrincipal() instanceof StaffAccountPrincipal principal) {
                Long storeId = principal.getStoreId();
                if (storeId != null) {
                    storeRepository.findById(storeId).ifPresent(store -> {
                        model.addAttribute("storeName", store.getName());
                        model.addAttribute("storeSlug", store.getSlug());
                    });
                }
            }
        }

        if (!model.containsAttribute("storeName")) {
            model.addAttribute("storeName", storeName);
        }
        model.addAttribute("username", username);
        model.addAttribute("activeNav", "home");

        return "admin/home";
    }
}
