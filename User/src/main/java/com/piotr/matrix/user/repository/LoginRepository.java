package com.piotr.matrix.user.repository;

import com.piotr.matrix.user.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginRepository extends JpaRepository<LoginEntity, UUID> {
    Optional<LoginEntity> findByEmail(String email);
}
