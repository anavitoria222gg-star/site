package com.site.backend.controller;

import com.site.backend.entity.Usuario;
import com.site.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/users")  // ← ALTERADO: de "/admin" para "/users"
public class UserController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @GetMapping("/list")  // ← ALTERADO: de "/users" para "/list"
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            model.addAttribute("users", usuarios);
            model.addAttribute("totalUsers", usuarios.size());
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar usuários: " + e.getMessage());
        }
        
        return "admin/users"; // Ou mude o template se necessário
    }
}