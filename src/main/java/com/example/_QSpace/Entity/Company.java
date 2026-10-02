package com.example._QSpace.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Company {

    @Id
    @Column(nullable = false, unique = true)
    private String deviceId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String companyEmail;

    private String companyLogo;

    @Column(nullable = false)
    private String phoneNumber;
}

