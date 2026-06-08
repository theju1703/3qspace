package com.example._QSpace.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Contactus {
    
    @Id
    private String email;
    
    private String message;
    
    @Column(nullable = false)
    private String name;
}