package com.piotr.matrix.auth.service;


import com.piotr.matrix.auth.generated.model.JwtTokenResponse;
import com.piotr.matrix.auth.generated.model.LoginRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImp implements AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.username}")
    private String adminUser;
    @Value("${admin.password}")
    private String adminPassword;

    @Autowired
    public AuthServiceImp(JwtService jwtService, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public JwtTokenResponse handleUserValidation(LoginRequest loginRequest) {
        if (adminUser.equals(loginRequest.getEmail()) && adminPassword.equals(loginRequest.getPassword())) {
            return jwtService.generateToken(loginRequest.getEmail(), loginRequest.getPassword(), "master");
        } else {
            authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            return jwtService.generateToken(loginRequest.getEmail(), loginRequest.getPassword(), "");
        }

    }

    private void authenticate(@NotNull @Email String email, @NotNull String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
