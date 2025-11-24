package com.piotr.matrix.auth.service;

import com.piotr.matrix.auth.generated.model.JwtTokenResponse;
import com.piotr.matrix.auth.generated.model.LoginRequest;

public interface AuthService {

    JwtTokenResponse handleUserValidation(LoginRequest  loginRequest);
}
