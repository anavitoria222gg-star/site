package com.site.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("titulo", "Página Inicial");
        return "index";
    }
    
    @GetMapping("/home")
    public String homePage(Model model) {
        // Verificar se o usuário está autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  
        
        // Adicionar informações do usuário ao modelo
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> 
                    grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        
        model.addAttribute("titulo", "Área do Usuário");
        model.addAttribute("mensagem", "Bem-vindo à sua área pessoal!");
        model.addAttribute("username", username);
        model.addAttribute("isAdmin", isAdmin);
        
        return "home";
    }
    
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("titulo", "Login");
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("titulo", "Cadastro");
        return "register";
    }
    
    // Página de acesso negado
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("titulo", "Acesso Negado");
        return "error/access-denied";
    }
    
    // Página de logout com feedback
    @GetMapping("/logout-success")
    public String logoutSuccess(Model model) {
        model.addAttribute("titulo", "Logout realizado");
        model.addAttribute("mensagem", "Você saiu do sistema com sucesso!");
        return "index"; // Retorna para a página inicial
    }
}