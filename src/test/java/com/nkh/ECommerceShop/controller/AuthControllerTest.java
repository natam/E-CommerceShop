package com.nkh.ECommerceShop.controller;

import com.google.gson.Gson;
import com.nkh.ECommerceShop.dto.auth.TokenRefreshRequestDTO;
import com.nkh.ECommerceShop.dto.auth.UserCredentialsDTO;
import com.nkh.ECommerceShop.exception.TokenRefreshException;
import com.nkh.ECommerceShop.model.RefreshToken;
import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.repository.RefreshTokenRepository;
import com.nkh.ECommerceShop.repository.UsersRepository;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.RefreshTokenService;
import com.nkh.ECommerceShop.security.service.UserDetailsImpl;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuthController.class, includeFilters = {
        // to include JwtUtil in spring context
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes={JwtUtils.class, AuthEntryPointJwt.class})})
@Import(WebSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    PasswordEncoder encoder;
    @MockBean
    private UsersRepository usersRepository;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    AuthenticationManager authenticationManager;
    @MockBean
    private RefreshTokenService tokenService;

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
        String email = "test@gmail.com";
        Users user = new Users("test", email, encoder.encode("password"), Role.USER);
        UserCredentialsDTO credentials = new UserCredentialsDTO(email, "password");
        given(userDetailsService.loadUserByUsername("test@gmail.com"))
                .willReturn(UserDetailsImpl.build(user));
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
                .andExpect(jsonPath("roles", is(List.of(Role.USER))))
                .andExpect(jsonPath("refreshToken", notNullValue()))
                .andExpect(jsonPath("accessToken", notNullValue()))
                .andExpect(jsonPath("user", is(email)));
    }

    @Test
    void givenNotExistingCredentials_ReturnError() throws Exception {
        UserCredentialsDTO credentials = new UserCredentialsDTO("test1@gmail.com", "password");
        given(userDetailsService.loadUserByUsername("test1@gmail.com"))
                .willThrow( new UsernameNotFoundException("Bad credentials"));
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
    void givenCorrectEmailAndWrongPassword_ReturnError() throws Exception {
        Users user = new Users("test", "test@gmail.com", encoder.encode("password123"), Role.USER);
        UserCredentialsDTO credentials = new UserCredentialsDTO("test@gmail.com", "password");
        given(userDetailsService.loadUserByUsername("test@gmail.com"))
                .willReturn(UserDetailsImpl.build(user));
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
        Users user = new Users("test22", "test22@gmail.com", encoder.encode("password"), Role.USER);
        TokenRefreshRequestDTO tokenRequest = new TokenRefreshRequestDTO();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().plusMillis(15000));
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        given(tokenService.findByToken(tokenValue)).willReturn(Optional.of(refreshToken));
        given(tokenService.verifyExpiration(refreshToken)).willReturn(refreshToken);
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
        TokenRefreshRequestDTO tokenRequest = new TokenRefreshRequestDTO();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().minusMillis(15000));
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        tokenRequest.setRefreshToken(tokenValue);
        given(tokenService.findByToken(tokenValue)).willReturn(Optional.of(refreshToken));
        given(tokenService.verifyExpiration(refreshToken)).willThrow(new TokenRefreshException(tokenValue, "Refresh token was expired. Please make a new signin request"));
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