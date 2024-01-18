package com.nkh.ECommerceShop.controller;

import com.google.gson.Gson;
import com.nkh.ECommerceShop.dto.auth.TokenRefreshRequestDTO;
import com.nkh.ECommerceShop.dto.auth.UserCredentialsDTO;
import com.nkh.ECommerceShop.model.RefreshToken;
import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.repository.RefreshTokenRepository;
import com.nkh.ECommerceShop.repository.UsersRepository;
import com.nkh.ECommerceShop.security.service.RefreshTokenService;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(usersRepository);
    private final RefreshTokenService tokenService = new RefreshTokenService(refreshTokenRepository, usersRepository);

    @Test
    void givenInvalidEmail_ReturnError() throws Exception {
        UserCredentialsDTO credentials = new UserCredentialsDTO("test", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Should be provided valid email")));
    }

    @Test
    void givenEmptyEmail_ReturnError() throws Exception {
        UserCredentialsDTO credentials = new UserCredentialsDTO("", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Email can not be empty")));
    }

    @Test
    void givenEmptyPassword_ReturnError() throws Exception {
        UserCredentialsDTO credentials = new UserCredentialsDTO("test@gmail.com", "");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Password can not be empty")));
    }

    @Test
    void givenValidCredentials_ReturnAuthCookie() throws Exception {
        Users user = new Users("test", "test@gmail.com", encoder.encode("password"), Role.USER);
        usersRepository.save(user);
        UserCredentialsDTO credentials = new UserCredentialsDTO("test@gmail.com", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("nkh"))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("You are logged in successfully"));
    }

    @Test
    void givenNotExistingCredentials_ReturnError() throws Exception {
        UserCredentialsDTO credentials = new UserCredentialsDTO("test1@gmail.com", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("nkh"))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Bad credentials")));
    }

    @Test
    void givenTokenIsNotInDB_ReturnError() throws Exception {
        TokenRefreshRequestDTO tokenRequest = new TokenRefreshRequestDTO();
        String token = UUID.randomUUID().toString();
        tokenRequest.setRefreshToken(token);
        String expectedErrorMessage = String.format("Failed for [%s]: %s", token, "Refresh token is not in database!");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/refreshtoken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(tokenRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(expectedErrorMessage)));
    }

    @Test
    void givenValidToken_ReturnAccessToken() throws Exception {
        Users user = usersRepository.save(new Users("test22", "test22@gmail.com", encoder.encode("password"), Role.USER));
        TokenRefreshRequestDTO tokenRequest = new TokenRefreshRequestDTO();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().plusMillis(15000));
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        refreshTokenRepository.save(refreshToken);
        tokenRequest.setRefreshToken(tokenValue);
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/refreshtoken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(tokenRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken", notNullValue()))
                .andExpect(jsonPath("refreshToken", notNullValue()));
    }

    @Test
    void givenExpiredToken_ReturnError() throws Exception {
        Users user = new Users("test11", "test11@gmail.com", encoder.encode("password"), Role.USER);
        usersRepository.save(user);
        TokenRefreshRequestDTO tokenRequest = new TokenRefreshRequestDTO();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().minusMillis(15000));
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        refreshTokenRepository.save(refreshToken);
        tokenRequest.setRefreshToken(tokenValue);
        String expectedErrorMessage = String.format("Failed for [%s]: %s", tokenValue, "Refresh token was expired. Please make a new signin request");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/refreshtoken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(tokenRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(expectedErrorMessage)));
    }
}