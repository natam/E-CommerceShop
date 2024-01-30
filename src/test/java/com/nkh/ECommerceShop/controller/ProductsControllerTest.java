package com.nkh.ECommerceShop.controller;

import com.google.gson.*;
import com.nkh.ECommerceShop.exception.AlreadyExistsException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.AuthTokenFilter;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import com.nkh.ECommerceShop.service.ProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(value = ProductsController.class, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes={AuthEntryPointJwt.class})})
@Import(WebSecurityConfig.class)
class ProductsControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    AuthEntryPointJwt authEntryPointJwt;
    @MockBean
    ProductsRepository productsRepository;
    @MockBean
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
    @WithMockUser(authorities = "ADMIN")
    void givenAdminAndValidProduct_Return200OnCreate() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        given(productsService.createProduct(product)).willReturn(product);
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("product1")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenProductWithNameAndPriceExistingInDB_ReturnError400() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        String errorMessage = String.format("Product with name %s and price %f already exists",
                product.getName(), product.getPrice());
        given(productsService.createProduct(product))
                .willThrow(new AlreadyExistsException(errorMessage));
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void givenCreateValidProductAsUser_ReturnError403OnCreate() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("message",is("Access Denied - you don't have permissions for this action")));
    }

    @Test
    void givenNoTokenAndValidProduct_ReturnError401OnCreate() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        mvc.perform(
                post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(product))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message", is("Full authentication is required to access this resource")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenUpdateProductThatNotInDB_ReturnError404() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        long productId = 1L;
        String errorMessage = String.format("Product with id %s was not found",
                productId);
        given(this.productsService.updateProduct(productId, product))
                .willThrow(new ResourceNotFoundException(errorMessage));
        mvc.perform(
                        put("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(product))
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenUpdateProduct_Return200() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        long productId = 1L;
        product.setId(productId);
        given(this.productsService.updateProduct(productId, product))
                .willReturn(product);
        mvc.perform(
                        put("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(product))
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is(product.getName())));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void givenGetProductThatNotInDB_ReturnError404() throws Exception {
        long productId = 1L;
        String errorMessage = String.format("Product with id %s was not found",
                productId);
        given(productsService.getById(productId))
                .willThrow(new ResourceNotFoundException(errorMessage));
        mvc.perform(
                        get("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void givenGetProductWithValidId_Return200AndProduct() throws Exception {
        long productId = 1L;
        Product product = new Product("product1", "testing product", 5.50, 5);
        product.setId(productId);
        given(productsService.getById(productId))
                .willReturn(product);
        mvc.perform(
                        get("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is(product.getName())));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenDeleteProductThatNotInDB_ReturnError404() throws Exception {
        long productId = 1L;
        String errorMessage = String.format("Product with id %s was not found",
                productId);
        given(this.productsService.deleteProduct(productId))
                .willThrow(new ResourceNotFoundException(errorMessage));
        mvc.perform(
                        delete("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenDeleteWithValidProductId_Return200() throws Exception {
        long productId = 1L;
        String message = String.format("Product with id %s was deleted",
                productId);
        given(this.productsService.deleteProduct(productId))
                .willReturn(true);
        mvc.perform(
                        delete("/api/v1/products/" + productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenRequestedValidPageNumber_ReturnProducts() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        List<Product> content = new ArrayList<>();
        content.add(product);
        Page<Product> products = new PageImpl<>(content, PageRequest.of(0,4),1);
        given(productsService.getAllProducts(0,4)).willReturn(products);
        mvc.perform(
                        get("/api/v1/products?page=0&size=4")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalProducts", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenRequestedPageNumberThatIsEmpty_ReturnError() throws Exception {
        String errorMessage = "Page 2 not found. Products has 1 pages";
        given(productsService.getAllProducts(2,4)).willThrow(new ResourceNotFoundException(errorMessage));
        mvc.perform(
                        get("/api/v1/products?page=2&size=4")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message", is(errorMessage)));
    }
}