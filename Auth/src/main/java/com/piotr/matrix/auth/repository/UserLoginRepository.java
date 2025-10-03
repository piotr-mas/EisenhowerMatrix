package com.piotr.matrix.auth.repository;

import com.piotr.matrix.auth.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLoginRepository extends JpaRepository<LoginEntity, String> {
    Optional<LoginEntity> findByEmail(String email);

}
