package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.exception.PlaceOrderException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderProduct;
import com.nkh.ECommerceShop.model.order.OrderStatus;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import com.nkh.ECommerceShop.repository.OrderStatusesRepository;
import com.nkh.ECommerceShop.repository.OrdersStatusesHistoryRepository;
import com.nkh.ECommerceShop.security.WebSecurityConfig;
import com.nkh.ECommerceShop.security.jwt.AuthEntryPointJwt;
import com.nkh.ECommerceShop.security.jwt.JwtUtils;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import com.nkh.ECommerceShop.service.OrdersService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    OrderStatusesRepository orderStatusesRepository;
    @MockBean
    OrdersStatusesHistoryRepository statusesHistoryRepository;

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

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenGetMyOrderWithValidOrderId_ReturnOrderDTO() throws Exception {
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
        when(ordersService.getMyOrderById(2)).thenReturn(order);
        mvc.perform(
                        get("/api/v1/orders/my/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(2)))
                .andExpect(jsonPath("id", is(2)))
                .andExpect((jsonPath("totalOrderSum", is(20.0))))
                .andExpect((jsonPath("products", hasSize(1))))
                .andExpect((jsonPath("products[0].productQuantity", is(2))))
                .andExpect((jsonPath("products[0].product.id", is(1))))
                .andExpect((jsonPath("lastStatus.status.id", is(0))));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenGetMyOrderWithNotValidOrderId_ReturnError() throws Exception {
        String errorMessage = "Order with id 2 not found";
        when(ordersService.getMyOrderById(2)).thenThrow(new ResourceNotFoundException(errorMessage));
        mvc.perform(
                        get("/api/v1/orders/my/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "ADMIN")
    void givenGetOrderWithValidOrderId_ReturnOrderDTO() throws Exception {
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
        when(ordersService.getOrderById(2)).thenReturn(order);
        mvc.perform(
                        get("/api/v1/orders/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(2)))
                .andExpect(jsonPath("id", is(2)))
                .andExpect((jsonPath("totalOrderSum", is(20.0))))
                .andExpect((jsonPath("products", hasSize(1))))
                .andExpect((jsonPath("products[0].productQuantity", is(2))))
                .andExpect((jsonPath("products[0].product.id", is(1))))
                .andExpect((jsonPath("lastStatus.status.id", is(0))));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "ADMIN")
    void givenGetOrderWithNotValidOrderId_ReturnError() throws Exception {
        String errorMessage = "Order with id 2 not found";
        when(ordersService.getOrderById(2)).thenThrow(new ResourceNotFoundException(errorMessage));
        mvc.perform(
                        get("/api/v1/orders/2")
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
    void givenGetOrderWithNotAdmin_ReturnError() throws Exception {
        String errorMessage = "Access Denied - you don't have permissions for this action";
        mvc.perform(
                        get("/api/v1/orders/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void givenUpdateOrderStatusWithNotAdmin_ReturnError() throws Exception {
        String errorMessage = "Access Denied - you don't have permissions for this action";
        mvc.perform(
                        post("/api/v1/orders/2/status")
                                .param("newStatusId", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "ADMIN")
    void givenUpdateOrderStatusWithNotValidStatus_ReturnError() throws Exception {
        String errorMessage = "Status with id 6 not found in DB";
        doThrow(new ResourceNotFoundException(errorMessage)).when(ordersService).updateOrderStatus(2,6);
        mvc.perform(
                        post("/api/v1/orders/2/status")
                                .param("newStatusId", "6")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "ADMIN")
    void givenUpdateOrderStatusWithValidStatus_ReturnOk() throws Exception {
        String message = "Order status was updated";
        OrderStatus status = new OrderStatus(2, "processing", "testing");
        when(orderStatusesRepository.findById(2L)).thenReturn(Optional.of(status));
        OrderStatusHistory statusHistory = new OrderStatusHistory(2,status);
        when(statusesHistoryRepository.save(Mockito.any(OrderStatusHistory.class))).thenReturn(statusHistory);
        mvc.perform(
                        post("/api/v1/orders/2/status")
                                .param("newStatusId", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(message)));
    }
}