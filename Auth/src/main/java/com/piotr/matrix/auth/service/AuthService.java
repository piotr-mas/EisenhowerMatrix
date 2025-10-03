package com.piotr.matrix.auth.service;

import com.piotr.matrix.auth.generated.model.JwtTokenResponse;
import com.piotr.matrix.auth.generated.model.LoginRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthService {

    JwtTokenResponse handleUserValidation(LoginRequest  loginRequest);

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
