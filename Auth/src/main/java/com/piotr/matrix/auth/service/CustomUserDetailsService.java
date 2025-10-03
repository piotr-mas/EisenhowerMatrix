package com.piotr.matrix.auth.service;

import com.piotr.matrix.auth.entity.LoginEntity;
import com.piotr.matrix.auth.exception.UserNotFoundException;
import com.piotr.matrix.auth.repository.UserLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserLoginRepository userLoginRepository;

    @Autowired
    public CustomUserDetailsService(UserLoginRepository userLoginRepository) {
        this.userLoginRepository = userLoginRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws AuthenticationException {
        // Lookup user from DB or in-memory store
        LoginEntity user = userLoginRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // hashed
                .roles(user.getRole())        // e.g., "ADMIN"
                .build();
    }
}
