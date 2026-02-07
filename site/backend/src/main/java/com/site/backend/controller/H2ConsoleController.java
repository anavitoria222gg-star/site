package com.site.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class H2ConsoleController {
    
    @GetMapping("/admin/h2-console")
    public String h2ConsoleRedirect() {
        // Verificar se é admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && 
            auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            
            // É admin, redirecionar para H2 Console
            return "redirect:/h2-console";
        }
        
        // Não é admin, redirecionar para acesso negado
        return "redirect:/access-denied";
    }
}