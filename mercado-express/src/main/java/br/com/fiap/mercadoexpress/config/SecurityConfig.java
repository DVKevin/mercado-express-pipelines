package br.com.fiap.mercadoexpress.config;

import br.com.fiap.mercadoexpress.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** API REST (Postman/Insomnia): HTTP Basic, sem sessão e sem CSRF. */
    @Bean
    @Order(1)
    SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/produtos/**").permitAll()
                        .requestMatchers("/api/dashboard/**", "/api/emails/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /** Interface Web: login próprio, cadastro e rotas por perfil. */
    @Bean
    @Order(2)
    SecurityFilterChain webChain(HttpSecurity http, UsuarioRepository usuarios) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/index.html", "/login", "/cadastro",
                                "/css/**", "/img/**", "/error").permitAll()
                        .requestMatchers("/admin/**", "/fornecedores/**").hasRole("ADMIN")
                        .requestMatchers("/produtos/novo", "/produtos/*/editar", "/produtos/*/excluir",
                                "/produtos/salvar").hasRole("ADMIN")
                        .requestMatchers("/produtos/**", "/carrinho/**", "/pedidos/**").hasAnyRole("CLIENTE", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .successHandler(redirecionarPorPerfil())
                        .failureHandler(falhaNoLogin(usuarios))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"));
        return http.build();
    }

    /** Depois do login cada perfil vai para sua área restrita. */
    private AuthenticationSuccessHandler redirecionarPorPerfil() {
        return (request, response, authentication) -> {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(request.getContextPath() + (admin ? "/admin/dashboard" : "/produtos"));
        };
    }

    /** Se o e-mail não existe, manda para o cadastro (Sign Up); senão, volta ao login com erro. */
    private AuthenticationFailureHandler falhaNoLogin(UsuarioRepository usuarios) {
        return (request, response, exception) -> {
            String email = request.getParameter("email");
            if (email != null && !email.isBlank() && !usuarios.existsByEmailIgnoreCase(email.trim())) {
                response.sendRedirect(request.getContextPath() + "/cadastro?naoEncontrado&email="
                        + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(request.getContextPath() + "/login?error");
            }
        };
    }
}
