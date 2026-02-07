// Adicione ao seu AuthController.java ou crie um novo ApiController.java
package com.site.backend.controller;

import com.site.backend.entity.Usuario;
import com.site.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }
    
    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
    }
}