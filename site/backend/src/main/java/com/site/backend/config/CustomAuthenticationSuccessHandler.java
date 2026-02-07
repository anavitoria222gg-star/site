package com.site.backend.config;

import com.site.backend.entity.Usuario;
import com.site.backend.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;

    public CustomAuthenticationSuccessHandler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        String email = authentication.getName();
        System.out.println("✅ Login bem-sucedido para: " + email);

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            response.sendRedirect("/login?error=true");
            return;
        }

        System.out.println("🎭 Role do usuário: " + usuario.getRole());

        if ("ADMIN".equalsIgnoreCase(usuario.getRole())) {
            System.out.println("🚨 Admin detectado! Redirecionando para /admin/admindashboard");
            response.sendRedirect("/admin/admindashboard"); // ✅ CORRIGIDO
        } else {
            System.out.println("👤 Usuário comum. Redirecionando para /home");
            response.sendRedirect("/home");
        }
    }
}
