package com.piotr.matrix.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
// Added import for Base64 decoding
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
// Secret property MUST match the key used for signing below
@TestPropertySource(properties = {"jwt.secret=YV92ZXJ5X3NlY3VyZV9zZWNyZXRfa2V5X29mXzMyX2J5dGVzX29yX21vcmU="})
class JwtAuthFilterIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    private WireMockServer wireMockServer;

    // INJECTION: Get the exact secret string that Spring passed to the JwtAuthFilter
    @Value("${jwt.secret}")
    private String secretForTestSigning;


    @BeforeEach
    void setupWireMock() {
        // WireMock is set up to run on port 9999
        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();

        // Stubbing an external call the Gateway would make
        wireMockServer.stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"id\":1,\"name\":\"Piotr\"}")
                        .withHeader("Content-Type", "application/json")));
    }

    @AfterEach
    void teardownWireMock() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

   /* @Test//NOT WORKING - NEED a FIX
    void shouldForwardRequestWithValidJwt() {
        byte[] decodedKeyBytes = Decoders.BASE64.decode(secretForTestSigning);
        Key signingKey = Keys.hmacShaKeyFor(decodedKeyBytes);

        String token = Jwts.builder()
                .setSubject("user123")
                .claim("roles", "ADMIN")
                .signWith(signingKey)
                .compact();

        webTestClient.get()
                .uri("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Piotr");
    }

    @Test
    void shouldRejectRequestWithInvalidJwt() {
        // Test with an invalid token (missing signature)
        String invalidToken = Jwts.builder()
                .setSubject("user123")
                .claim("roles", "ADMIN")
                .compact();

        webTestClient.get()
                .uri("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldRejectRequestWithMissingJwt() {
        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }*/
}
