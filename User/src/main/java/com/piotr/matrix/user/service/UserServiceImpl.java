package com.piotr.matrix.user.service;

import com.piotr.matrix.auth.generated.model.*;
import com.piotr.matrix.user.entity.LoginEntity;
import com.piotr.matrix.user.entity.UserEntity;
import com.piotr.matrix.user.repository.LoginRepository;
import com.piotr.matrix.user.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Log4j2
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, LoginRepository loginRepository) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    @Override
    public RegisterUserResponse registerUser(RegisterUserRequest user) {
        log.debug("Registering user {}", user.getEmail());
        var loginEntity = new LoginEntity(user.getEmail(), user.getRole());
        var userEntity = new UserEntity(user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getDob(), user.getPreferredQuadrant());

        var loginDataSaved = loginRepository.save(loginEntity);
        var userDataSaved = userRepository.save(userEntity);

        var userName = new UserName(userDataSaved.getFirstName(),  userDataSaved.getLastName());
        return new RegisterUserResponse(userName, loginDataSaved.getRole());
    }

    @Override
    public UserResponse getUserById(UUID id) {
        var userDataFromDb =  userRepository.findById(id).orElse(null);
        if (userDataFromDb != null) {
            var userDataLoginFromDb = loginRepository.findById(userDataFromDb.getEmail()).orElse(null);
            if (userDataLoginFromDb != null) {
                var userResponse = new UserResponse();
                userResponse.setId(userDataFromDb.getId());
                userResponse.setFirstName(userDataFromDb.getFirstName());
                userResponse.setLastName(userDataFromDb.getLastName());
                userResponse.setDob(String.valueOf(userDataFromDb.getBirthDate()));
                userResponse.setEmail(userDataFromDb.getEmail());
                userResponse.setPreferredQuadrant(userDataFromDb.getPreferredQuadrant());
                userResponse.setRole(userDataLoginFromDb.getRole());
                userResponse.setCreatedAt(userDataFromDb.getCreatedAt().toString());
                return userResponse;
            }
        }
        return null;
    }
}
