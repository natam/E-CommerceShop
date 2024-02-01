package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.exception.NotEnoughProductQuantityException;
import com.nkh.ECommerceShop.model.*;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import com.nkh.ECommerceShop.service.CartsService;
import com.nkh.ECommerceShop.service.ProductsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CartsController.class, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes={AuthEntryPointJwt.class})})
@Import(WebSecurityConfig.class)
class CartsControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    AuthEntryPointJwt authEntryPointJwt;
    @MockBean
    JwtUtils jwtUtils;
    @MockBean
    ProductsService productsService;
    @MockBean
    CartsService cartsService;
    @MockBean
    UserDetailsServiceImpl userDetailsService;
    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenAddProductThatNotInCartYet_ReturnOk() throws Exception {
        Product product2 = new Product("product2", "testing product", 4.30, 15);
        Cart cart = new Cart(1);
        CartProduct cartProduct1 = new CartProduct(0, product2, 3);
        cart.getCartProducts().add(cartProduct1);
        cart.setTotalCartProductsPrice(product2.getPrice()*3);
        given(cartsService.getMyCart()).willReturn(cart);
        given(productsService.checkProductStock(1,3)).willReturn(true);
        Product product = new Product("product1", "testing product", 5.50, 5);
        product.setId(1);
        given(productsService.getById(1)).willReturn(product);
        mvc.perform(
                        post("/api/v1/carts/mycart/products/add")
                                .param("productId", "1")
                                .param("quantity","4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Product was added to your cart")));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenAddProductThatExistsInCart_ReturnOk() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        product.setId(1);
        Cart cart = new Cart(1);
        CartProduct cartProduct1 = new CartProduct(0, product, 3);
        cart.getCartProducts().add(cartProduct1);
        cart.setTotalCartProductsPrice(product.getPrice()*3);
        given(cartsService.getMyCart()).willReturn(cart);
        given(productsService.checkProductStock(1,2)).willReturn(true);
        given(productsService.getById(1)).willReturn(product);
        mvc.perform(
                        post("/api/v1/carts/mycart/products/add")
                                .param("productId", "1")
                                .param("quantity","2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is("Product was added to your cart")));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenAddProductInCartWithNotEnoughQuantityInStock_Return400() throws Exception {
        Product product = new Product("product1", "testing product", 5.50, 5);
        String errorMessage = String.format("Product not added to cart: Not enough product quantity in stock. Available quantity is %d",product.getStock());
        given(cartsService.addProductToCart(1,6)).willThrow(new NotEnoughProductQuantityException(product.getStock()));
        mvc.perform(
                        post("/api/v1/carts/mycart/products/add")
                                .param("productId", "1")
                                .param("quantity","6")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }
}