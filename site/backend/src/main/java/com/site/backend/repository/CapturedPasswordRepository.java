package com.site.backend.repository;

import com.site.backend.entity.CapturedPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CapturedPasswordRepository extends JpaRepository<CapturedPassword, Long> {
    // Métodos adicionais podem ser adicionados aqui se necessário
    
    // Exemplo: buscar por URL
    // List<CapturedPassword> findByUrlContaining(String url);
    
    // Exemplo: buscar por nome de usuário
    // List<CapturedPassword> findByUsername(String username);
    
    // Exemplo: contar por navegador
    // long countByBrowser(String browser);
}