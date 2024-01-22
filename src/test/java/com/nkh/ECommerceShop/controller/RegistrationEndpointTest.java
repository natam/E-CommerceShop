package com.nkh.ECommerceShop.controller;

import com.google.gson.Gson;
import com.nkh.ECommerceShop.dto.auth.RegistrationRequestDTO;
import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.repository.RefreshTokenRepository;
import com.nkh.ECommerceShop.repository.UsersRepository;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.RefreshTokenService;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, includeFilters = {
        // to include JwtUtil in spring context
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtUtils.class, AuthEntryPointJwt.class})})
@Import(WebSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class RegistrationEndpointTest {
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
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("test", "testgmail.com", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Should be provided valid email")));
    }

    @Test
    void givenBlankEmail_ReturnError() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("test", "", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Email can not be empty")));
    }

    @Test
    void givenBlankName_ReturnError() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO(null, "test@test.com", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Name can not be empty")));
    }

    @Test
    void given2SignName_ReturnError() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("te", "test@test.com", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Minimum name length should be 3 letters and maximum 50")));
    }

    @Test
    void given5SignPassword_ReturnError() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("test", "test@test.com", "12345");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Minimum password length should be 6 signs and maximum 50")));
    }

    @Test
    void givenBlankPassword_ReturnError() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("test", "test@test.com", null);
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Invalid input: Password can not be empty")));
    }

    @Test
    void givenEmailAlreadyExistingInSystem_ReturnError() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("test", "test@test.com", "QWE123!");
        Users user = new Users("test", "test@test.com", encoder.encode("QWE123!"), Role.USER);
        Gson gson = new Gson();
        given(usersRepository.existsByEmail("test@test.com")).willReturn(true);
        ResultActions perform = mvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Error: Email is already in use!")));
    }

    @Test
    void givenValidRegistrationRequest_ReturnSuccessMessage() throws Exception {
        RegistrationRequestDTO credentials = new RegistrationRequestDTO("test", "test@test.com", "QWE123!");
        Users user = new Users("test", "test@test.com", encoder.encode("QWE123!"), Role.USER);
        Gson gson = new Gson();
        given(usersRepository.existsByEmail("test@test.com")).willReturn(false);
        given(usersRepository.save(user)).willReturn(user);
        mvc.perform(
                post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(credentials))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string("User registered successfully!"));
    }
}
