package com.clothingrental.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Regras de acesso da aplicação.
 *
 * <p>A ordem das regras importa: o Spring Security usa a primeira que combinar com a URL.
 * CSRF e as proteções de sessão (como a troca do ID de sessão após o login)
 * permanecem com os padrões seguros do framework.
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/",
        "/login",
        "/catalog/**",
        "/garments/**",
        "/rental-requests/**",
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
                .requestMatchers(HttpMethod.GET, PUBLIC_PATHS).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                // Vai para /admin, ou para a página protegida que o usuário tentou abrir antes do login.
                .defaultSuccessUrl("/admin"))
            .logout(logout -> logout
                // Com CSRF habilitado, o logout só é aceito via POST em /logout.
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
