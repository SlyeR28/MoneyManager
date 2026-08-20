package org.moneymanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Security.Jwt.JwtUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    @Test
    @DisplayName("generateToken and extractEmail should generate and correctly extract the email subject")
    void testGenerateTokenAndExtractEmail() {
        String email = "testuser@example.com";
        String token = jwtUtils.generateToken(email);

        assertThat(token).isNotBlank();
        String extractedEmail = jwtUtils.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("isTokenValid should return true for matching email and unexpired token")
    void testIsTokenValid_True() {
        String email = "validuser@example.com";
        String token = jwtUtils.generateToken(email);

        boolean valid = jwtUtils.isTokenValid(token, email);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid should return false when email does not match token subject")
    void testIsTokenValid_WrongEmail() {
        String email = "validuser@example.com";
        String token = jwtUtils.generateToken(email);

        boolean valid = jwtUtils.isTokenValid(token, "otheruser@example.com");
        assertThat(valid).isFalse();
    }
}
