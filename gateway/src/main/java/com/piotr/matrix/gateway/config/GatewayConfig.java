package com.piotr.matrix.gateway.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
public class GatewayConfig {

    // Inject the properties that worked in the legacy server config
    @Value("file:/app/ssl/gateway-keystore.p12")
    private Resource keyStore;
    @Value("${GATEWAY_STORE_PASSWORD}")
    private String keyStorePassword;
    @Value("file:/app/ssl/gateway-truststore.jks")
    private Resource trustStore;
    @Value("${GATEWAY_TRUST_PASSWORD}")
    private String trustStorePassword;

    @Bean
    public HttpClient webClientSslHttpClient() throws IOException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, UnrecoverableKeyException {

        // --- 1. Load Key Manager Factory (Client Identity) ---
        // Gateway uses its own keystore (gateway-keystore.p12) to present its cert to Auth/User.
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keyStore.getFile()), keyStorePassword.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.toCharArray());


        // --- 2. Load Trust Manager Factory (Server Trust) ---
        // Gateway uses its truststore (gateway-truststore.jks) to verify Auth/User's cert.
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(trustStore.getFile()), trustStorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        // --- 3. Create SSL Context ---
        SslContext sslContext = SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK) // Recommended for wide compatibility
                .keyManager(kmf)             // Client identity (Gateway's certificate)
                .trustManager(tmf)           // Trust manager (CA for microservices)
                .build();

        // --- 4. Configure Netty HttpClient ---
        return HttpClient.create()
                .secure(ssl -> ssl.sslContext(sslContext)
                        // ⚠️ FIX: Explicitly disable hostname verification for internal calls
                        .handlerConfigurator(sslHandler -> {
                            SSLEngine engine = sslHandler.engine();
                            SSLParameters params = engine.getSSLParameters();
                            // Setting this to an empty string disables the automatic hostname verification.
                            params.setEndpointIdentificationAlgorithm("");
                            engine.setSSLParameters(params);
                        })
                );
    }
}
