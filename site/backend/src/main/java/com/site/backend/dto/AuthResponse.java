package com.site.backend.dto;

public class AuthResponse {
    private String nome;
    private String email;
    private String role;
    private String mensagem;
    
    public AuthResponse() {}
    
    public AuthResponse(String nome, String email, String role, String mensagem) {
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.mensagem = mensagem;
    }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
