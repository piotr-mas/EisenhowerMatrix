package com.piotr.matrix.user.repository;

import com.piotr.matrix.user.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<LoginEntity, String> {
}
