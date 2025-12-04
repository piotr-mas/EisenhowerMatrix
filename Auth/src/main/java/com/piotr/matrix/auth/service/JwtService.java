package com.piotr.matrix.auth.service;


import com.piotr.matrix.generated.model.JwtTokenResponse;

import java.util.UUID;

public interface JwtService {
    JwtTokenResponse generateToken(String username, String password, String role, UUID userId);
}
