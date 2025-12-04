package com.piotr.matrix.user.repository;

import com.piotr.matrix.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByIdAndEmail(UUID id, String email);

    Optional<UserEntity> findByEmail(String email);
}
