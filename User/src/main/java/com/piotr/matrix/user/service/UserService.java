package com.piotr.matrix.user.service;

import com.piotr.matrix.user.generated.model.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface UserService {

    URI registerUser(UserRegistration registerUserRequest);
    List<UserProfile> getAllUsers();
    UserProfile getUserById(UUID id);
    void deleteUser(UUID id);
    URI updateUser(UUID id, UserUpdate userUpdate);
    UserLoginResponse getUserLoginDetails(String email);
    URI changeUserEmail(UUID id, UserEmailRequest userEmailRequest);
    URI changeUserPassword(UUID id, UserPasswordRequest userPasswordRequest);
    URI changeUserRole(UUID id, UserRoleRequest userRoleRequest);
}
