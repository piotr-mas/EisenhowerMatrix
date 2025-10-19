package com.piotr.matrix.user.controller;

import com.piotr.matrix.auth.generated.api.UsersApi;
import com.piotr.matrix.auth.generated.model.RegisterUserRequest;
import com.piotr.matrix.auth.generated.model.RegisterUserResponse;
import com.piotr.matrix.auth.generated.model.UserResponse;
import com.piotr.matrix.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Log4j2
@RestController
public class UserController implements UsersApi {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserResponse> getUser(UUID userId) {
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<RegisterUserResponse> registerUser(RegisterUserRequest registerUserRequest) {
        RegisterUserResponse registerUserResponse = userService.registerUser(registerUserRequest);
        return ResponseEntity.ok(registerUserResponse);
    }
}
