package com.piotr.matrix.user.entity;

import com.piotr.matrix.generated.model.Role;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "login", uniqueConstraints = {
        @UniqueConstraint(name="Login_Email", columnNames = "email")
})
public class LoginEntity {
    @Id
    @Setter
    private String email;
    @Setter
    private String password;
    @Setter()
    @Enumerated(EnumType.STRING)
    private Role role;
}
