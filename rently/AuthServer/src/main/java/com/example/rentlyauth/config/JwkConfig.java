package com.example.rentlyauth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;


@Configuration
public class JwkConfig {

    private static final Logger LOGGER = Logger.getLogger(JwkConfig.class.getName());

    @Value("${authserver.jwk.keystore.path}")
    private Resource keyStoreResource;

    @Value("${authserver.jwk.keystore.password}")
    private String keyStorePassword;

    @Value("${authserver.jwk.key.alias}")
    private String keyAlias;

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] password = keyStorePassword.toCharArray();

            try (InputStream is = keyStoreResource.getInputStream()) {
                keyStore.load(is, password);
            }

            KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(password);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, entryPassword);

            if (privateKeyEntry == null) {
                String errorMessage = "Nie można znaleźć klucza o aliasie '" + keyAlias + "' w pliku keystore: " + keyStoreResource.getDescription();
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new IllegalStateException(errorMessage);
            }

            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("authserver-key")
                    .build();

            JWKSet jwkSet = new JWKSet(rsaKey);

            return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);

        } catch (Exception e) {
            String errorMessage = "Błąd podczas ładowania JWKSource z pliku keystore: "
                    + (keyStoreResource != null ? keyStoreResource.getDescription() : "ścieżka nieustawiona");
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new IllegalStateException("Nie można zainicjalizować JWKSource", e);
        }
    }
}