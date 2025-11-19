package com.piotr.matrix.user.service;

import com.piotr.matrix.user.entity.LoginEntity;
import com.piotr.matrix.user.entity.UserEntity;
import com.piotr.matrix.user.exception.InvalidClientRequestException;
import com.piotr.matrix.user.exception.UserNotFoundException;
import com.piotr.matrix.user.generated.model.*;
import com.piotr.matrix.user.repository.LoginRepository;
import com.piotr.matrix.user.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ToDo: Validate user and allow only authorized users access to the resources
 */
@Log4j2
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private static final String EMAIL =  "Email: ";
    private static final String USER_ID = "UserId: ";
    private static final String USER_PATH = "/user/";


    @Autowired
    public UserServiceImpl(UserRepository userRepository, LoginRepository loginRepository) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    @Override
    @Transactional
    public URI registerUser(UserRegistration user) {
        var hashedPassword = getHashedPassword(user.getPassword());
        var email = user.getUser().getEmail();
        var role = user.getUser().getRole();
        log.debug("Registering user {}", email);
        var loginEntity = new LoginEntity(email, hashedPassword, role);
        loginRepository.save(loginEntity);

        var firstName = user.getUser().getFirstName();
        var lastName = user.getUser().getLastName();
        var userEntity = new UserEntity(null, firstName, lastName, email, null);


        var newUser = userRepository.save(userEntity);
        return URI.create(USER_PATH +newUser.getId());
    }

    private String getHashedPassword(String password) {
        if  (password == null) {
            password = PasswordGenerator.generatedPassword();
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @Override
    public List<UserProfile> getAllUsers() {
        return userRepository.findAll().stream()
                .map(entity -> {
                    var email = entity.getEmail();
                    var loginEntity = loginRepository.findByEmail(email)
                            .orElseThrow(() -> new UserNotFoundException(EMAIL+ email));
                    var user  = new User(entity.getFirstName(), entity.getLastName(), email, loginEntity.getRole());
                   return new UserProfile(entity.getId(), user, entity.getCreatedAt());
                }).toList();
    }

    @Override
    public UserProfile getUserById(UUID id) {
        var userEntity =  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_ID+ id));
        var loginEntity = loginRepository.findByEmail(userEntity.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(EMAIL+ userEntity.getEmail()));

        var user = new User();
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setEmail(userEntity.getEmail());
        user.setRole(loginEntity.getRole());
        var userProfile = new UserProfile();
        userProfile.setId(userEntity.getId());
        userProfile.setUser(user);
        userProfile.setCreatedAt(userEntity.getCreatedAt());
        return userProfile;
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
        loginRepository.deleteById(id);
    }

    @Override
    @Transactional
    public URI updateUser(UUID id, UserUpdate userUpdate) {
        var user = Optional.ofNullable(userUpdate.getUser())
                .orElseThrow(() -> new InvalidClientRequestException("Invalid user update"));

        var userDataFromDb =  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_ID+ id));
        userDataFromDb.setFirstName(user.getFirstName());
        userDataFromDb.setLastName(user.getLastName());
        userDataFromDb.setEmail(user.getEmail());
        userRepository.save(userDataFromDb);

        var loginEntity = loginRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException(EMAIL+ user.getEmail()));
        loginEntity.setRole(user.getRole());
        loginRepository.save(loginEntity);
        return URI.create(USER_PATH  +id);
    }

    @Override
    public UserLoginResponse getUserLoginDetails(String email) {
        var loginEntity = loginRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(EMAIL + email));
        var loginResponse = new UserLoginResponse();
        loginResponse.setPassword(loginEntity.getPassword());
        loginResponse.setRole(loginEntity.getRole());
        return loginResponse;
    }

    @Override
    @Transactional
    public URI changeUserEmail(UUID id, UserEmailRequest userEmailRequest) {
        log.info("changeUserEmail, old email: {}", userEmailRequest.getOldEmail());
        //validate if user with Id has valid email
        if (userRepository.existsByIdAndEmail(id, userEmailRequest.getOldEmail())) {
            var loginEntity =  loginRepository.findByEmail(userEmailRequest.getOldEmail())
                    .orElseThrow(() -> new UserNotFoundException(EMAIL + userEmailRequest.getOldEmail()));
            loginEntity.setEmail(userEmailRequest.getNewEmail());
            loginRepository.save(loginEntity);

            var userEntity = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(USER_ID + id));
            userEntity.setEmail(userEmailRequest.getNewEmail());
            userRepository.save(userEntity);
            return URI.create(USER_PATH + id);
        } else {
            throw new UserNotFoundException(USER_ID + id + EMAIL + userEmailRequest.getOldEmail());
        }
    }

    @Override
    @Transactional
    public URI changeUserPassword(UUID id, UserPasswordRequest userPasswordRequest) {
        log.info("changeUserPassword id {}", id);
        var userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_ID + id));
        var loginEntity =  loginRepository.findByEmail(userEntity.getEmail())
                .orElseThrow(() -> new UserNotFoundException(EMAIL + userEntity.getEmail()));

        if (loginEntity.getPassword().equals(userPasswordRequest.getOldPassword())) {
            var hashedPassword = getHashedPassword(userPasswordRequest.getNewPassword());
            loginEntity.setPassword(hashedPassword);
            loginRepository.save(loginEntity);
            return URI.create(USER_PATH + id);
        } else {
            throw new InvalidClientRequestException("Incorrect old password");
        }
    }

    @Override
    @Transactional
    public URI changeUserRole(UUID id, UserRoleRequest userRoleRequest) {
        log.info("changeUserRole id {}", id);
        var userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_ID + id));
        var loginEntity =  loginRepository.findByEmail(userEntity.getEmail())
                .orElseThrow(() -> new UserNotFoundException(EMAIL + userEntity.getEmail()));
        if (loginEntity.getRole().equals(userRoleRequest.getOldRole())) {
            loginEntity.setRole(userRoleRequest.getNewRole());
            loginRepository.save(loginEntity);
            return URI.create(USER_PATH + id);
        } else {
            throw new InvalidClientRequestException("Incorrect old role "+userRoleRequest.getOldRole());
        }

    }


    static class PasswordGenerator {
        private static final SecureRandom random = new SecureRandom();
        private PasswordGenerator() {}
        static String generatedPassword() {
            return random.ints(8, 32, 122)
                    .mapToObj(i -> (char) i)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        }
    }
}
