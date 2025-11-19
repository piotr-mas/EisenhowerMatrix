package com.piotr.matrix.user.service;

import com.piotr.matrix.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl.PasswordGenerator  passwordGenerator;
    @Mock
    private UserRepository userRepository;

    @Test
    void whenGeneratePassword_returnPassword() {
        var password = passwordGenerator.generatedPassword();
        assertNotNull(password);
    }
}