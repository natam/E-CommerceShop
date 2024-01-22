package com.nkh.ECommerceShop.security.service;

import com.nkh.ECommerceShop.exception.TokenRefreshException;
import com.nkh.ECommerceShop.model.RefreshToken;
import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.repository.RefreshTokenRepository;
import com.nkh.ECommerceShop.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.rmi.server.UID;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository tokenRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private RefreshTokenService tokenService;

    @Test
    void findByToken() {
        String tokenValue = UUID.randomUUID().toString();
        Users user = new Users("test", "email", "pwTest123", Role.USER);
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setId(1);
        token.setExpiryDate(Instant.now().plusMillis(60*1000));
        Mockito.when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        RefreshToken receivedToken = tokenService.findByToken(tokenValue).get();
        assertEquals(token, receivedToken);
    }
    @Test
    void whenTokenIsNotExpiredReturnToken() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setExpiryDate(Instant.now().plusMillis(60*1000));
        assertEquals(token, tokenService.verifyExpiration(token));
    }

    @Test
    void whenTokenExpiredThrowException() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setExpiryDate(Instant.now().minusMillis(1000));
        Exception exception = assertThrows(TokenRefreshException.class, () -> tokenService.verifyExpiration(token));
        String expectedMessage = String.format("Failed for [%s]: %s", tokenValue, "Refresh token was expired. Please make a new signin request");
        assertEquals(expectedMessage, exception.getMessage());
    }
}