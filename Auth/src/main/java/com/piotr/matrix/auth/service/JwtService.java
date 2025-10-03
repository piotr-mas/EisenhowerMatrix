package com.piotr.matrix.auth.service;

import com.piotr.matrix.auth.generated.model.JwtTokenResponse;

public interface JwtService {
    JwtTokenResponse generateToken(String username, String password, String role);
}
