package com.tech10x.ukm.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES attribute converter for columns the doc marks "Encrypted at rest"
 * (e.g. SUPPLIER.bank_account_no). Applied via {@code @Convert(converter = CryptoConverter.class)}.
 * <p>
 * Registered as a Spring bean so {@code app.crypto.secret} can be injected -
 * Spring Boot's Hibernate auto-configuration wires JPA converters through the
 * Spring bean container automatically, the same way {@code @Value} works in
 * {@code JwtService}.
 */
@Converter
@Component
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";

    @Value("${app.crypto.secret}")
    private String secret;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec());
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encrypt field for storage", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decrypt field from storage", e);
        }
    }

    private SecretKeySpec keySpec() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] key = Arrays.copyOf(sha256.digest(secret.getBytes(StandardCharsets.UTF_8)), 16); // AES-128 key
        return new SecretKeySpec(key, ALGORITHM);
    }
}
