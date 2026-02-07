package com.site.backend.dto;

public class UsuarioComSenhaDTO {
    private Long id;
    private String nome;
    private String email;
    private String senha; // hash
    private String senhaTexto; // texto
    private String role;
    
    // Construtor
    public UsuarioComSenhaDTO(Long id, String nome, String email, String senha, String senhaTexto, String role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.senhaTexto = senhaTexto;
        this.role = role;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getSenhaTexto() { return senhaTexto; }
    public void setSenhaTexto(String senhaTexto) { this.senhaTexto = senhaTexto; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}