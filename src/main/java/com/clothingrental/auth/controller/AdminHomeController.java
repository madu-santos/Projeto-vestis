package com.clothingrental.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página administrativa provisória, usada apenas para comprovar o login.
 * Será substituída quando o painel administrativo for construído.
 */
@Controller
public class AdminHomeController {

    @GetMapping("/admin")
    public String home() {
        return "admin/home";
    }
}
