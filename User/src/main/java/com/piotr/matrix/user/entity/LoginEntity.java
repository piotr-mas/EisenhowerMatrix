package com.piotr.matrix.user.entity;


import com.piotr.matrix.auth.generated.model.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class LoginEntity {
    @Id
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public LoginEntity(@NotNull @Email String email, Role userRole) {
        this.email = email;
        this.role = userRole;
        this.password = "todo";
    }
}
