package com.piotr.matrix.auth.controller;

import com.piotr.matrix.auth.generated.api.AuthApi;
import com.piotr.matrix.auth.service.AuthService;
import com.piotr.matrix.generated.model.JwtTokenResponse;
import com.piotr.matrix.generated.model.LoginRequest;
import com.piotr.matrix.generated.model.RefreshTokenRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<JwtTokenResponse> loginUser(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.handleUserValidation(loginRequest));
    }

    @Override
    public ResponseEntity<Void> logoutUser() {
        return AuthApi.super.logoutUser();
    }

    @Override
    public ResponseEntity<JwtTokenResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return AuthApi.super.refreshToken(refreshTokenRequest);
    }
}
