package com.piotr.matrix.user.controller;

import com.piotr.matrix.generated.model.*;
import com.piotr.matrix.user.generated.api.DefaultApi;
import com.piotr.matrix.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
public class UserController implements DefaultApi {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<Void> addUser(UserRegistration userRegistration) {
        log.info("addUser: {}", userRegistration.getUser().getEmail());
        return ResponseEntity.created(userService.registerUser(userRegistration))
                .build();
    }

    @Override
    public ResponseEntity<Void> changeEmail(UUID id, UserEmailRequest userEmailRequest) {
        log.info("changeEmail id: {}", id);
        return ResponseEntity.accepted()
                .location(userService.changeUserEmail(id, userEmailRequest))
                .build();
    }

    @Override
    public ResponseEntity<Void> changePassword(UUID id, UserPasswordRequest userPasswordRequest) {
        log.info("changePassword id: {}", id);
        return ResponseEntity.accepted()
                .location(userService.changeUserPassword(id, userPasswordRequest))
                .build();
    }

    @Override
    public ResponseEntity<Void> changeRole(UUID id, UserRoleRequest userRoleRequest) {
        log.info("changeRole id: {}",id);
        return ResponseEntity.accepted()
                .location(userService.changeUserRole(id, userRoleRequest))
                .build();
    }

    @Override
    public ResponseEntity<Void> deleteUserById(UUID id) {
        log.info("deleteUserById: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        log.info("getAllUsers");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Override
    public ResponseEntity<UserProfile> getUserById(UUID id) {
        log.info("getUserById: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<Void> updateUserById(UUID id, UserUpdate userUpdate) {
        log.info("updateUserById: {}", userUpdate);
        return ResponseEntity.accepted().location(userService.updateUser(id, userUpdate)).build();
    }

    @Override
    public ResponseEntity<UserLoginResponse> getUserPassword(Email email) {
        log.info("getUserPassword: {}", email);
        return ResponseEntity.ok().body(userService.getUserLoginDetails(email.getEmail()));
    }
}
