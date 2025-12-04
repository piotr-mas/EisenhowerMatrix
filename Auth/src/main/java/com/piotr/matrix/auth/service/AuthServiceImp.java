package com.piotr.matrix.auth.service;


import com.piotr.matrix.generated.model.JwtTokenResponse;
import com.piotr.matrix.generated.model.LoginRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AuthServiceImp implements AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUser;
    @Value("${admin.password}")
    private String adminPassword;

    @Autowired
    public AuthServiceImp(JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JwtTokenResponse handleUserValidation(LoginRequest loginRequest) {
        log.info("Login Request: {}.", loginRequest);
        if (adminUser.equals(loginRequest.getEmail()) && adminPassword.equals(loginRequest.getPassword()) &&
                passwordEncoder.matches(loginRequest.getPassword(), adminPassword)) {
            return jwtService.generateToken(loginRequest.getEmail(), loginRequest.getPassword(), "master", null);
        } else {
            authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            String role = getRoleFromSecurityContextHolder();
            return jwtService.generateToken(loginRequest.getEmail(), loginRequest.getPassword(), role, null);
        }

    }

    private String getRoleFromSecurityContextHolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                // Spring Security prefixes roles with "ROLE_". We must remove this.
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .map(auth -> auth.substring("ROLE_".length()).toLowerCase())
                // Should not happen, but safe fallback
                .orElse("user");
    }

    private void authenticate(@NotNull @Email String email, @NotNull String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
