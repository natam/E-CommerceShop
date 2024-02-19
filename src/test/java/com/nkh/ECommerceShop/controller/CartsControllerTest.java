package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.exception.NotEnoughProductQuantityException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.*;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.UserDetailsImpl;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenGetMyCartWithSeveralProducts_ReturnOkAndCart() throws Exception {
        Product product2 = new Product("product2", "testing product", 14.30, 15);
        product2.setId(2);
        Product product1 = new Product("product1", "testing product", 5.50, 5);
        product1.setId(1);
        Cart cart = new Cart(1);
        cart.setId(3);
        CartProduct cartProduct1 = new CartProduct(3, product1, 1);
        cartProduct1.setId(1);
        CartProduct cartProduct2 = new CartProduct(3, product2, 2);
        cartProduct2.setId(2);
        cart.getCartProducts().add(cartProduct1);
        cart.getCartProducts().add(cartProduct2);
        double totalPrice = product2.getPrice()*2 + product1.getPrice();
        cart.setTotalCartProductsPrice(totalPrice);
        given(cartsService.getMyCart()).willReturn(cart);
        Users user = new Users("test", "test@test.com", "pass1234", Role.USER);
        user.setId(1);
        given(userDetailsService.loadUserByUsername("test@test.com")).willReturn(UserDetailsImpl.build(user));
        mvc.perform(
                        get("/api/v1/carts/mycart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(3)))
                .andExpect(jsonPath("totalCartProductsPrice", is(totalPrice)))
                .andExpect(jsonPath("cartProducts", hasSize(2)))
                .andExpect(jsonPath("cartProducts[0].product.id", is(1)))
                .andExpect(jsonPath("cartProducts[0].productQuantity", is(1)))
                .andExpect(jsonPath("cartProducts[1].product.id", is(2)))
                .andExpect(jsonPath("cartProducts[1].productQuantity", is(2)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenReduceProductThatNotInCart_Return404() throws Exception {
        String errorMessage = "Product with id 3 is not found in cart";
        doThrow(new ResourceNotFoundException(errorMessage)).when(cartsService).reduceProductQuantityInCart(3);
        mvc.perform(
                        post("/api/v1/carts/mycart/products/reduce")
                                .param("productId", "3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenReduceProductWithQuantity2InCart_ReturnOk() throws Exception {
        Product product2 = new Product("product2", "testing product", 14.30, 15);
        product2.setId(2);
        Cart cart = new Cart(1);
        cart.setId(3);
        CartProduct cartProduct2 = new CartProduct(0, product2, 2);
        cart.getCartProducts().add(cartProduct2);
        double totalPrice = product2.getPrice()*2;
        cart.setTotalCartProductsPrice(totalPrice);
        given(cartsService.getMyCart()).willReturn(cart);
        Users user = new Users("test", "test@test.com", "pass1234", Role.USER);
        user.setId(1);
        given(userDetailsService.loadUserByUsername("test@test.com")).willReturn(UserDetailsImpl.build(user));
        String message = String.format("Product with id %d was reduced", 2);
        mvc.perform(
                        post("/api/v1/carts/mycart/products/reduce")
                                .param("productId", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenReduceProductWithQuantity1InCart_ReturnOk() throws Exception {
        Product product1 = new Product("product1", "testing product", 5.50, 5);
        product1.setId(1);
        Cart cart = new Cart(1);
        CartProduct cartProduct1 = new CartProduct(0, product1, 1);
        cart.getCartProducts().add(cartProduct1);
        double totalPrice = product1.getPrice();
        cart.setTotalCartProductsPrice(totalPrice);
        Users user = new Users("test", "test@test.com", "pass1234", Role.USER);
        user.setId(1);
        given(cartsService.getMyCart()).willReturn(cart);
        given(userDetailsService.loadUserByUsername("test@test.com")).willReturn(UserDetailsImpl.build(user));
        String message = String.format("Product with id %d was reduced", 1);
        mvc.perform(
                        post("/api/v1/carts/mycart/products/reduce")
                                .param("productId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenDeleteProductFromCart_ReturnOk() throws Exception {
        Product product1 = new Product("product1", "testing product", 5.50, 5);
        product1.setId(1);
        Cart cart = new Cart(1);
        CartProduct cartProduct1 = new CartProduct(0, product1, 1);
        cart.getCartProducts().add(cartProduct1);
        double totalPrice = product1.getPrice();
        cart.setTotalCartProductsPrice(totalPrice);
        Users user = new Users("test", "test@test.com", "pass1234", Role.USER);
        user.setId(1);
        given(cartsService.getMyCart()).willReturn(cart);
        given(userDetailsService.loadUserByUsername("test@test.com")).willReturn(UserDetailsImpl.build(user));
        String message = String.format("Product with id %d was removed from cart", 1);
        mvc.perform(
                        delete("/api/v1/carts/mycart/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenClearCart_ReturnOk() throws Exception {
        Product product1 = new Product("product1", "testing product", 5.50, 5);
        product1.setId(1);
        Cart cart = new Cart(1);
        CartProduct cartProduct1 = new CartProduct(0, product1, 1);
        cart.getCartProducts().add(cartProduct1);
        double totalPrice = product1.getPrice();
        cart.setTotalCartProductsPrice(totalPrice);
        Users user = new Users("test", "test@test.com", "pass1234", Role.USER);
        user.setId(1);
        given(cartsService.getMyCart()).willReturn(cart);
        given(userDetailsService.loadUserByUsername("test@test.com")).willReturn(UserDetailsImpl.build(user));
        String message = "Cart was cleaned";
        mvc.perform(
                        post("/api/v1/carts/mycart/clear")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }
}