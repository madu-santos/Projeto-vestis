package com.clothingrental.institutional.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelo site institucional da plataforma Vestis (GET /).
 *
 * <p>Exibe a página pública de apresentação do sistema, detalhando a proposta de valor,
 * recursos de gestão para lojas de aluguel de roupas, arquitetura multi-tenant
 * e formulário para solicitação de demonstração.
 */
@Controller
public class InstitutionalController {

    @GetMapping("/")
    public String home() {
        return "institutional/home";
    }
}
