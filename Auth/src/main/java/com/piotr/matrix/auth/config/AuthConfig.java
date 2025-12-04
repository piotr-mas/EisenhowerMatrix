package com.piotr.matrix.auth.config;

import com.piotr.matrix.auth.exception.TlsException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.*; // Added necessary imports
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class AuthConfig {

    @Value("${url.ms-user}")
    private String userUrl;

    /**
     * Create a standard WebClient for local HTTP connection
     * This bean is loaded when the 'docker' profile IS NOT active
     */
    @Bean
    @Profile("!" + "docker")
    public WebClient.Builder userWebClientBuilder() {
        return WebClient.builder().baseUrl(userUrl);
    }

    /**
     * Creates a WebClient.Builder with mTLS configured for HTTPS connections.
     * This bean is loaded ONLY when the 'docker' profile IS active.
     */
    @Bean
    @Profile("docker")
    public WebClient.Builder userWebClientBuilderMutualTls(
            @Value("${server.ssl.key-store}") Resource keyStore,
            @Value("${server.ssl.key-store-password}") String keyStorePassword,
            @Value("${server.ssl.trust-store}") Resource trustStore,
            @Value("${server.ssl.trust-store-password}") String trustStorePassword) throws SSLException {

        // --- 1. Load TrustStore and create custom TrustManager (TS remains the same) ---
        KeyStore ts;
        try {
            ts = KeyStore.getInstance("JKS");
            try (InputStream tsIs = trustStore.getInputStream()) {
                ts.load(tsIs, trustStorePassword.toCharArray());
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            final X509TrustManager originalTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];

            // Define the custom TrustManager that delegates but skips identity check
            X509TrustManager noHostCheckTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    originalTrustManager.checkClientTrusted(chain, authType);
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // This check validates the CA, but Java's standard implementation
                    // of X509TrustManagerImpl.checkServerTrusted calls checkIdentity,
                    // which is what throws the hostname exception.
                    // By defining a custom manager, we often bypass the strict identity check.
                    originalTrustManager.checkServerTrusted(chain, authType);
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return originalTrustManager.getAcceptedIssuers();
                }
            };

            // --- 2. Load KeyStore (KS remains the same) ---
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream ksIs = keyStore.getInputStream()) {
                ks.load(ksIs, keyStorePassword.toCharArray());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyStorePassword.toCharArray());

            // --- 3. Build Netty SSL Context for mTLS ---
            SslContext sslContext = SslContextBuilder.forClient()
                    .keyManager(kmf)
                    // FIX #1: Pass the custom TrustManager array correctly
                    .trustManager(noHostCheckTrustManager)
                    .build();

            // --- 4. Create an HttpClient with the secure SSL Context ---
            HttpClient httpClient = HttpClient.create()
                    .secure(sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext)
                            // FIX #2: Explicitly disable hostname verification algorithm on the SSLEngine
                            .handlerConfigurator(sslHandler -> {
                                SSLEngine engine = sslHandler.engine();
                                SSLParameters params = engine.getSSLParameters();
                                params.setEndpointIdentificationAlgorithm(null); // This is the key fix
                                engine.setSSLParameters(params);
                            })
                    );

            // --- 5. Configure WebClient to use the secure HttpClient ---
            return WebClient.builder()
                    .baseUrl(userUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient));

        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            // Consolidated exception handling for readability
            throw new TlsException("TLS Configuration Error: " + e.getMessage());
        }
    }
}