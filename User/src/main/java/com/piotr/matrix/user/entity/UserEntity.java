package com.piotr.matrix.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name="User_Email", columnNames = "email")
})
public class UserEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Column(unique = true)
    @Setter
    private String email;
    @CreationTimestamp
    private LocalDateTime createdAt;

}
