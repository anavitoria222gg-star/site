package com.site.backend.config;

import com.site.backend.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                         CustomAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
        
        http
            .authorizeHttpRequests(auth -> auth
                // ====== URLs PÚBLICAS (sem login) - Usando MvcRequestMatcher ======
                .requestMatchers(mvc.pattern("/")).permitAll()
                .requestMatchers(mvc.pattern("/index")).permitAll()
                .requestMatchers(mvc.pattern("/index.html")).permitAll()
                .requestMatchers(mvc.pattern("/login")).permitAll()
                .requestMatchers(mvc.pattern("/register")).permitAll()
                .requestMatchers(mvc.pattern("/css/**")).permitAll()
                .requestMatchers(mvc.pattern("/js/**")).permitAll()
                .requestMatchers(mvc.pattern("/images/**")).permitAll()
                .requestMatchers(mvc.pattern("/favicon.ico")).permitAll()
                .requestMatchers(mvc.pattern("/logout-success")).permitAll()
                .requestMatchers(mvc.pattern("/error")).permitAll()
                .requestMatchers(mvc.pattern("/error/**")).permitAll()
                .requestMatchers(mvc.pattern("/access-denied")).permitAll()
                .requestMatchers(mvc.pattern("/api/auth/**")).permitAll()
                .requestMatchers(mvc.pattern("/api/test")).permitAll()
                .requestMatchers(mvc.pattern("/api/health")).permitAll()
                
                // ====== H2 CONSOLE - SOMENTE ADMIN (usando AntPathRequestMatcher) ======
                .requestMatchers(new AntPathRequestMatcher("/h2-console")).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).hasRole("ADMIN")
                
                // ====== PÁGINAS QUE EXIGEM AUTENTICAÇÃO ======
                .requestMatchers(mvc.pattern("/home")).authenticated()
                .requestMatchers(mvc.pattern("/profile")).authenticated()
                .requestMatchers(mvc.pattern("/dashboard")).authenticated()
                
                // ====== ROTAS ADMIN ======
                .requestMatchers(mvc.pattern("/admin/**")).hasRole("ADMIN")
                
                // ====== PÁGINAS DE USUÁRIO COMUM ======
                .requestMatchers(mvc.pattern("/user/**")).authenticated()
                
                // Todo resto precisa de autenticação
                .anyRequest().authenticated()
            )
            
            // Configuração do login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // Configuração do userDetailsService
            .userDetailsService(userDetailsService)
            
            // Configuração do logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Tratamento de acesso negado
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
            )
            
            // Configurações para H2 Console
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            )
            
            // CSRF - desabilitar para H2 Console funcionar
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/h2-console/**"),
                    new AntPathRequestMatcher("/api/**")
                )
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}