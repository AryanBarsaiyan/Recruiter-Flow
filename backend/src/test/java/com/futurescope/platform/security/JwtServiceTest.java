package com.futurescope.platform.security;

import com.futurescope.platform.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JWT token generation, parsing, and validation.
 */
class JwtServiceTest {

    private static final String TEST_SECRET_BASE64 = "ZmFrZV9mYWtlX2Zha2VfZmFrZV9mYWtlX2Zha2VfZmFrZV9mYWtlX2Zha2U=";
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET_BASE64, 60L);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setUserType("recruiter");
    }

    @Test
    void generateAccessToken_returnsNonEmptyToken() {
        UUID sessionId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(user, sessionId);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUserId_returnsCorrectUserId() {
        UUID sessionId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(user, sessionId);
        String subject = jwtService.extractUserId(token);
        assertEquals(user.getId().toString(), subject);
    }

    @Test
    void extractSessionId_returnsCorrectSessionId() {
        UUID sessionId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(user, sessionId);
        UUID extracted = jwtService.extractSessionId(token);
        assertEquals(sessionId, extracted);
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateAccessToken(user, UUID.randomUUID());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.signature"));
    }

    @Test
    void isTokenValid_tokenSignedWithDifferentKey_returnsFalse() {
        JwtService otherService = new JwtService("b3RoZXJfa2V5X290aGVyX2tleV9vdGhlcl9rZXlfb3RoZXJfa2V5X29r", 60L);
        String token = otherService.generateAccessToken(user, UUID.randomUUID());
        assertFalse(jwtService.isTokenValid(token));
    }
}
