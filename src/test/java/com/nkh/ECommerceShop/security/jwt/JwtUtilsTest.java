package com.nkh.ECommerceShop.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JwtUtils.class)
class JwtUtilsTest {
    private final int jwtExpirationMs = 120000;
    private final String jwtCookie = "nkh";
    private final String jwtSecret = "d0hucVhzRTVDbW5FUVk2RURwQ2ctUk45NE9rMzNlbmdUdVRtdndvRzlVcw==";
    private final JwtUtils jwtUtils = new JwtUtils(jwtCookie, jwtExpirationMs, jwtSecret);
    @BeforeEach
    void setUp() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb3JvemtvLm5ubkBnbWFpbC5jb20iLCJpYXQiOjE3MDU0MTk1NTEsImV4cCI6MTcwNTQxOTYxMX0.oJRjrB21r7HoGFX1LmCnWnGKt_GB8TfEd-dDnpQlmqs";
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getUserNameFromJwtToken() {
        String username = "natam123@test.com";
        String jwt = jwtUtils.generateTokenFromUsername(username);
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(jwt);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateJwtToken() {
    }
}