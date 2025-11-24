package com.piotr.matrix.auth.service;

import com.piotr.matrix.auth.exception.UserNotFoundException;
import com.piotr.matrix.user.generated.model.Email;
import com.piotr.matrix.user.generated.model.UserLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final WebClient msUserClient;

    @Autowired
    public CustomUserDetailsService(WebClient.Builder userClient) {
        this.msUserClient = userClient.build();
    }

    /**
     * The AuthenticationManager looks up the user using the UserDetailsService.loadUserByUsername() method.
     * @param userEmail
     * @return
     * @throws AuthenticationException
     */
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws AuthenticationException {
        Email email = new Email(userEmail);
        UserLoginResponse user = msUserClient.post()
                .uri("/password")
                .bodyValue(email)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(UserNotFoundException.class)
                                .map(e -> new UserNotFoundException("Error 400: User not found in ms-user")))
                .bodyToMono(UserLoginResponse.class)
                .block();
        if (user == null) {
            throw new UserNotFoundException("User not found in ms-user");
        }

        return User.builder()
                .username(userEmail)
                .password(user.getPassword()) // hashed
                .roles(user.getRole().getValue())
                .build();
    }
}
