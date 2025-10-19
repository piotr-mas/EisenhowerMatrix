package com.piotr.matrix.user.service;

import com.piotr.matrix.auth.generated.model.RegisterUserRequest;
import com.piotr.matrix.auth.generated.model.RegisterUserResponse;
import com.piotr.matrix.auth.generated.model.UserResponse;

import java.util.UUID;

public interface UserService {

    RegisterUserResponse registerUser(RegisterUserRequest registerUserRequest);
    UserResponse getUserById(UUID id);
}
