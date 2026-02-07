package com.site.backend.controller;

import com.site.backend.dto.AuthResponse;
import com.site.backend.dto.LoginRequest;
import com.site.backend.dto.RegisterRequest;
import com.site.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // ← ADICIONE ESTE IMPORT
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    // 🔧 ENDPOINT DE HEALTH CHECK
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ API Auth funcionando!");
    }
    
    // 🔧 ENDPOINT DE LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                                  HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.login(request);
            
            // Criar sessão
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("usuario", response);
            
            // 🔥 DEFINIR REDIRECIONAMENTO POR ROLE
            String redirectUrl;
            String role = response.getRole();
            
            if (role != null && role.equalsIgnoreCase("ADMIN")) {
                redirectUrl = "/admin/dashboard";  // Admin vai para dashboard
            } else {
                redirectUrl = "/home";  // Usuários comuns vão para home
            }
            
            System.out.println("🔥 Login de: " + response.getEmail() + 
                              " | Role: " + role + 
                              " | Redirecionando para: " + redirectUrl);
            
            return ResponseEntity.ok()
                .header("X-Redirect", redirectUrl)
                .body(response);
                
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                .body("{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // 🔧 NOVO ENDPOINT: Login com redirecionamento automático (302)
    @PostMapping("/login-redirect")
    public void loginRedirect(@RequestBody LoginRequest request,
                             HttpServletRequest httpRequest,
                             HttpServletResponse httpResponse) throws Exception {
        try {
            AuthResponse response = authService.login(request);
            
            // Criar sessão
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("usuario", response);
            
            // 🔥 REDIRECIONAMENTO IMEDIATO (302)
            if ("ADMIN".equalsIgnoreCase(response.getRole())) {
                httpResponse.sendRedirect("/admin/dashboard");
            } else {
                httpResponse.sendRedirect("/home");
            }
            
        } catch (RuntimeException e) {
            httpResponse.sendRedirect("/login?error=" + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("{\"mensagem\": \"Logout realizado\"}");
    }
    
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            AuthResponse usuario = (AuthResponse) session.getAttribute("usuario");
            if (usuario != null) {
                return ResponseEntity.ok(usuario);
            }
        }
        return ResponseEntity.status(401).build();
    }
    
    // 🔧 ENDPOINT PARA TESTE
    @GetMapping("/test-session")
    public ResponseEntity<String> testSession(HttpSession session) {
        AuthResponse usuario = (AuthResponse) session.getAttribute("usuario");
        
        if (usuario == null) {
            return ResponseEntity.ok("Nenhum usuário na sessão");
        }
        
        return ResponseEntity.ok(
            "Usuário: " + usuario.getEmail() + 
            "<br>Role: " + usuario.getRole() + 
            "<br>É admin? " + ("ADMIN".equalsIgnoreCase(usuario.getRole()) ? "SIM" : "NÃO")
        );
    }
}