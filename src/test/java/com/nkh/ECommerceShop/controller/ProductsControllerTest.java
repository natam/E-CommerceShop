package com.nkh.ECommerceShop.controller;

import com.google.gson.*;
import com.nkh.ECommerceShop.exception.AlreadyExistsException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.UserDetailsImpl;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import com.nkh.ECommerceShop.service.ProductsService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ProductsController.class, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtUtils.class, AuthEntryPointJwt.class})})
@Import(WebSecurityConfig.class)
@AutoConfigureMockMvc
class ProductsControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    JwtUtils jwtUtils;
    @MockBean
    ProductsService productsService;
    @MockBean
    UserDetailsServiceImpl userDetailsService;
    Gson gson;

    @BeforeEach
    public void initTest() {
        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, type, jsonDeserializationContext) ->
                        ZonedDateTime.parse(json.getAsJsonPrimitive()
                                .getAsString()).toLocalDateTime())
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (localDate, type, jsonSerializationContext) ->
                                new JsonPrimitive(formatter.format(localDate)))
                .create();
    }

    @Test
    void givenAdminAndValidProduct_Return200OnCreate() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl.build(
                        new Users("customer@test.com", "customer@test.com", "password", Role.ADMIN)));
        given(this.productsService.createProduct(product)).willReturn(product);
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .cookie(new Cookie("nkh", token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("product1")));
    }

    @Test
    void givenProductWithNameAndPriceExistingInDB_ReturnError400() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        String errorMessage = String.format("Product with name %s and price %f already exists",
                product.getName(), product.getPrice());
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.ADMIN)));
        given(this.productsService.createProduct(product))
                .willThrow(new AlreadyExistsException(errorMessage));
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .cookie(new Cookie("nkh", token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    void givenUpdateValidProductAsUser_ReturnError403OnCreate() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        String errorMessage = "Access Denied - you don't have permissions for this action";
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.USER)));
        given(this.productsService.createProduct(product))
                .willThrow(new AlreadyExistsException(errorMessage));
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .cookie(new Cookie("nkh", token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    void givenNoTokenAndValidProduct_ReturnError401OnCreate() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        String errorMessage = "Full authentication is required to access this resource";
        given(this.productsService.createProduct(product))
                .willThrow(new AlreadyExistsException(errorMessage));
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    void givenUpdateProductThatNotInDB_ReturnError404() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        long productId = 1L;
        String errorMessage = String.format("Product with id %s was not found",
                productId);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.ADMIN)));
        given(this.productsService.updateProduct(productId, product))
                .willThrow(new ResourceNotFoundException(errorMessage));
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                        put("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(product))
                                .cookie(new Cookie("nkh", token))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    void givenUpdateProduct_Return200() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        long productId = 1L;
        product.setId(productId);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.ADMIN)));
        given(this.productsService.updateProduct(productId, product))
                .willReturn(product);
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                        put("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(product))
                                .cookie(new Cookie("nkh", token))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is(product.getName())));
    }

    @Test
    void givenGetProductThatNotInDB_ReturnError404() throws Exception {
        long productId = 1L;
        String errorMessage = String.format("Product with id %s was not found",
                productId);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.USER)));
        given(this.productsService.getById(productId))
                .willThrow(new ResourceNotFoundException(errorMessage));
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                        get("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("nkh", token))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    void givenGetProductWithValidId_Return200AndProduct() throws Exception {
        long productId = 1L;
        Product product = new Product("product1", "testing product", 5.50, 5);
        product.setId(productId);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.USER)));
        given(this.productsService.getById(productId))
                .willReturn(product);
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                        get("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("nkh", token))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is(product.getName())));
    }

    @Test
    void givenDeleteProductThatNotInDB_ReturnError404() throws Exception {
        long productId = 1L;
        String errorMessage = String.format("Product with id %s was not found",
                productId);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.ADMIN)));
        given(this.productsService.deleteProduct(productId))
                .willThrow(new ResourceNotFoundException(errorMessage));
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                        delete("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("nkh", token))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    void givenDeleteWithValidProductId_Return200() throws Exception {
        long productId = 1L;
        String message = String.format("Product with id %s was deleted",
                productId);
        given(userDetailsService.loadUserByUsername("customer@test.com"))
                .willReturn(UserDetailsImpl
                        .build(new Users("customer@test.com", "customer@test.com", "password", Role.ADMIN)));
        given(this.productsService.deleteProduct(productId))
                .willReturn(true);
        String token = jwtUtils.generateTokenFromUsername("customer@test.com");
        mvc.perform(
                        delete("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("nkh", token))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }
}