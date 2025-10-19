package com.piotr.matrix.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class UserEntity {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    @JsonFormat(pattern = "dd-MM-yyyy") // For Jackson serialization/deserialization
    @DateTimeFormat(pattern = "dd-MM-yyyy") // For Spring Web MVC binding
    private LocalDate birthDate;
    private String preferredQuadrant;
    @Temporal(TemporalType.TIMESTAMP)
    private Instant createdAt;

    public UserEntity(@NotNull @Size(min = 2, max = 20) String firstName, @NotNull @Size(min = 2, max = 20) String lastName,
                      @NotNull @Email String email, @Pattern(regexp = "^\\\\d{2}-\\\\d{2}-\\\\d{4}$") String dob,
                      String preferredQuadrant) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = LocalDate.parse(dob);
        this.preferredQuadrant = preferredQuadrant;
        this.createdAt = Instant.now();
    }
}
