package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {
    private final OrdersService ordersService;

    @Autowired
    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/process/mycart")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Order> placeOrderFromMyCart(){
        return ResponseEntity.ok().body(ordersService.placeOrderFromCart());
    }
}
