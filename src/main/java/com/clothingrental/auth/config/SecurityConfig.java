package com.clothingrental.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Regras de acesso e segurança da plataforma Vestis.
 *
 * <p>A ordem das regras importa: o Spring Security usa a primeira que combinar com a URL.
 * Rotas públicas de vitrine e recursos estáticos aceitam apenas HTTP GET.
 * O envio público de solicitações de clientes aceita HTTP POST protegido por CSRF.
 * O painel administrativo (/admin/**) requer autenticação com papel ROLE_ADMIN.
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_GET_PATHS = {
        "/",
        "/login",
        "/lojas/**",
        "/css/**",
        "/js/**",
        "/images/**",
        "/favicon.ico",
        "/error"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Leitura pública: site institucional, vitrines e assets estáticos
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                // Envio público de solicitação de aluguel por clientes na vitrine (protegido por CSRF)
                .requestMatchers(HttpMethod.POST, "/lojas/*/solicitacoes").permitAll()
                // Qualquer outro método em /lojas/** (PUT, PATCH, DELETE, POSTs não previstos) é explicitamente negado,
                // inclusive para administradores autenticados
                .requestMatchers("/lojas/**").denyAll()
                // Área administrativa restrita aos funcionários das lojas
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Qualquer outro caminho exige autenticação
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin"))
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
