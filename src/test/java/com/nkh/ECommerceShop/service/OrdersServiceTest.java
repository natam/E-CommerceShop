package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.PlaceOrderException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderProduct;
import com.nkh.ECommerceShop.model.order.OrderStatus;
import com.nkh.ECommerceShop.repository.OrderStatusesRepository;
import com.nkh.ECommerceShop.repository.OrdersProductsRepository;
import com.nkh.ECommerceShop.repository.OrdersRepository;
import com.nkh.ECommerceShop.repository.OrdersStatusesHistoryRepository;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {
    @Mock
    CartsService cartsService;
    @Mock
    OrdersRepository ordersRepository;
    @Mock
    OrdersProductsRepository ordersProductsRepository;
    @Mock
    OrdersStatusesHistoryRepository ordersStatusesHistoryRepository;
    @Mock
    OrderStatusesRepository orderStatusesRepository;
    @Mock
    UserDetailsServiceImpl usersService;
    @Mock
    ProductsService productsService;
    @InjectMocks
    OrdersService ordersService;

    @Test
    void getMyOrders() {
        List<Order> userOrders = new ArrayList<>();
        Order order1 = new Order(1, 10);
        order1.setId(1);
        Product product1 = new Product("product1", "test description", 10, 20);
        product1.setId(1);
        OrderProduct orderProduct1 = new OrderProduct(1, product1, 1);
        order1.getProducts().add(orderProduct1);
        userOrders.add(order1);
        when(usersService.getCurrentUserId()).thenReturn(1L);
        when(ordersRepository.findByUserId(1L)).thenReturn(userOrders);
        assertEquals(userOrders, ordersService.getMyOrders());
    }

    @Test
    void givenPlaceOrderFromCartWithTwoProducts_ReturnOrder() {
        Product product1 = new Product("product1", "test description", 10, 20);
        product1.setId(1);
        when(usersService.getCurrentUserId()).thenReturn(1L);
        Cart myCart = new Cart(1L);
        myCart.setId(1);
        CartProduct cartProduct1 = new CartProduct(1, product1, 2);
        myCart.setTotalCartProductsPrice(product1.getPrice()*2);
        myCart.getCartProducts().add(cartProduct1);
        when(cartsService.getMyCart()).thenReturn(myCart);
        when(productsService.checkProductStock(1,2)).thenReturn(true);
        Order order = new Order(1,20);
        order.setId(2);
        OrderStatus status = new OrderStatus();
        status.setStatusName("processing");
        when(orderStatusesRepository.findById(1L)).thenReturn(Optional.of(status));
        when(ordersRepository.save(Mockito.any(Order.class))).thenReturn(order);
        Mockito.doAnswer(i -> {
            product1.setStock(product1.getStock()-2);
            return null;
        }).when(productsService).reduceProductStockQuantity(1,2);
        Order createdOrder = ordersService.placeOrderFromCart();
        assertEquals(1, createdOrder.getProducts().size());
        assertEquals(20, createdOrder.getTotalOrderSum());
        assertEquals(1, createdOrder.getTrackStatuses().size());
        assertTrue(createdOrder.getProducts().stream().anyMatch(orderProduct -> orderProduct.getProduct().equals(product1)));
        assertTrue(createdOrder.getProducts().stream().anyMatch(orderProduct -> orderProduct.getProductQuantity()==2));
        assertTrue(createdOrder.getTrackStatuses().stream().anyMatch(orderProduct -> orderProduct.getStatus().getStatusName().equals("processing")));
        assertEquals(18, product1.getStock());
    }

    @Test
    void givenPlaceOrderWithNotEnoughProductsInStock_ThrowException() {
        Product product1 = new Product("product1", "test description", 10, 20);
        product1.setId(1);
        String errorMessage = String.format("Order can not be placed. Not enough product (id: %d quantity in stock", product1.getId());
        when(usersService.getCurrentUserId()).thenReturn(1L);
        Cart myCart = new Cart(1L);
        myCart.setId(1);
        CartProduct cartProduct1 = new CartProduct(1, product1, 2);
        myCart.setTotalCartProductsPrice(product1.getPrice()*2);
        myCart.getCartProducts().add(cartProduct1);
        when(cartsService.getMyCart()).thenReturn(myCart);
        when(productsService.checkProductStock(1,2)).thenReturn(false);
        Exception exception = assertThrows(PlaceOrderException.class, () -> ordersService.placeOrderFromCart());
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void givenPlaceOrderFromEmptyCart_ThrowException() {
        String errorMessage = "Order can not be placed. Cart is empty.";
        when(usersService.getCurrentUserId()).thenReturn(1L);
        Cart myCart = new Cart(1L);
        myCart.setId(1);
        when(cartsService.getMyCart()).thenReturn(myCart);
        Exception exception = assertThrows(PlaceOrderException.class, () -> ordersService.placeOrderFromCart());
        assertEquals(errorMessage, exception.getMessage());
    }
}