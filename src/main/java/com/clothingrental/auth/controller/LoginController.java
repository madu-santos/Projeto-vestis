package com.clothingrental.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Exibe a página de login. O envio do formulário (POST /login) é processado
 * diretamente pelo filtro do Spring Security, por isso não há método para ele aqui.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
