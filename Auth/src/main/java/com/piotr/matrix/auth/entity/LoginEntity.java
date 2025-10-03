package com.piotr.matrix.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "user_login")
@Getter
public class LoginEntity {
    @Id
    private String email;
    private String password;
    private String role;
}
