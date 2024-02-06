package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.exception.PlaceOrderException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderProduct;
import com.nkh.ECommerceShop.model.order.OrderStatus;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import com.nkh.ECommerceShop.service.CartsService;
import com.nkh.ECommerceShop.service.OrdersService;
import com.nkh.ECommerceShop.service.ProductsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = OrdersController.class, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes={AuthEntryPointJwt.class})})
@Import(WebSecurityConfig.class)
class OrdersControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    AuthEntryPointJwt authEntryPointJwt;
    @MockBean
    JwtUtils jwtUtils;
    @MockBean
    OrdersService ordersService;
    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenPlaceOrderFromCartWithProduct_ReturnOrder() throws Exception {
        Product product1 = new Product("product1", "test description", 10, 20);
        product1.setId(1);
        OrderProduct orderProduct1 = new OrderProduct(1, product1, 2);
        Order order = new Order(1,20);
        order.setId(2);
        OrderStatus status = new OrderStatus();
        status.setStatusName("processing");
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setOrderId(2L);
        statusHistory.setStatus(status);
        order.getTrackStatuses().add(statusHistory);
        order.getProducts().add(orderProduct1);
        when(ordersService.placeOrderFromCart()).thenReturn(order);
        mvc.perform(
                        post("/api/v1/orders/process/mycart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(2)))
                .andExpect((jsonPath("totalOrderSum", is(20.0))))
                .andExpect((jsonPath("products", hasSize(1))))
                .andExpect((jsonPath("products[0].productQuantity", is(2))))
                .andExpect((jsonPath("products[0].product.id", is(1))))
                .andExpect((jsonPath("trackStatuses", hasSize(1))));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenPlaceOrderFromEmptyCart_ReturnError() throws Exception {
        String errorMessage = "Order can not be placed. Cart is empty.";
        when(ordersService.placeOrderFromCart()).thenThrow(new PlaceOrderException(errorMessage));
        mvc.perform(
                        post("/api/v1/orders/process/mycart")
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
    void givenPlaceOrderWithProductsWithNotEnoughQuantityInStock_ReturnError() throws Exception {
        String errorMessage = "Order can not be placed. Not enough product (id: 1) quantity in stock";
        when(ordersService.placeOrderFromCart()).thenThrow(new PlaceOrderException(errorMessage));
        mvc.perform(
                        post("/api/v1/orders/process/mycart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }
}