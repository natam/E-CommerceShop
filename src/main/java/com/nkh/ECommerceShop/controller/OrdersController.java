package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.dto.MessageResponseDTO;
import com.nkh.ECommerceShop.dto.OrderDTO;
import com.nkh.ECommerceShop.dto.OrdersPageDTO;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import com.nkh.ECommerceShop.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {
    private final OrdersService ordersService;
    private final UserDetailsServiceImpl usersService;

    @Autowired
    public OrdersController(OrdersService ordersService, UserDetailsServiceImpl usersService) {
        this.ordersService = ordersService;
        this.usersService = usersService;
    }

    @PostMapping("/process/mycart")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Order> placeOrderFromMyCart(){
        return ResponseEntity.ok().body(ordersService.placeOrderFromCart());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable("id") long orderId){
        Order order = ordersService.getOrderById(orderId);
        return ResponseEntity.ok().body(new OrderDTO(order));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<OrderDTO> getMyOrder(@PathVariable("id") long orderId){
        Order order = ordersService.getMyOrderById(orderId);
        return ResponseEntity.ok().body(new OrderDTO(order));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> updateOrderStatus(@PathVariable("id") long orderId,
                                                                @RequestParam("newStatusId") long newStatusId){
        ordersService.updateOrderStatus(orderId, newStatusId);
        return ResponseEntity.ok().body(new MessageResponseDTO("Order status was updated"));
    }

    @GetMapping("/{id}/track")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Set<OrderStatusHistory>> trackOrder(@PathVariable("id") long orderId){
        Order order = ordersService.getOrderById(orderId);
        return ResponseEntity.ok().body(order.getTrackStatuses());
    }

    @GetMapping("/my/{id}/track")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Set<OrderStatusHistory>> trackMyOrder(@PathVariable("id") long orderId){
        Order order = ordersService.getMyOrderById(orderId);
        return ResponseEntity.ok().body(order.getTrackStatuses());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<OrdersPageDTO> getMyOrders(@RequestParam("page") int page,
                                                     @RequestParam("size") int size,
                                                     @RequestParam(value = "startDate", required = false) String startDate,
                                                     @RequestParam(value = "endDate", required = false) String endDate,
                                                     @RequestParam(value = "orderSum", required = false) Integer orderSum){
        long currentUserId = usersService.getCurrentUserId();
        Page<Order> orders = ordersService.getAllOrders(page, size, startDate, endDate, orderSum, currentUserId);
        OrdersPageDTO ordersPage = new OrdersPageDTO();
        ordersPage.setOrders(orders.getContent());
        ordersPage.setTotalPages(orders.getTotalPages());
        ordersPage.setOffset(orders.getPageable().getOffset());
        ordersPage.setLimit(size);
        ordersPage.setCurrentPage(orders.getPageable().getPageNumber());
        ordersPage.setTotalOrders(orders.getNumberOfElements());
        return ResponseEntity.ok().body(ordersPage);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<OrdersPageDTO> getAllOrders(@RequestParam("page") int page,
                                                      @RequestParam("size") int size,
                                                      @RequestParam(value = "startDate", required = false) String startDate,
                                                      @RequestParam(value = "endDate", required = false) String endDate,
                                                      @RequestParam(value = "orderSum", required = false) Integer orderSum,
                                                      @RequestParam(value = "userId", required = false) Long userId){
        Page<Order> orders = ordersService.getAllOrders(page, size, startDate, endDate, orderSum, userId);
        OrdersPageDTO ordersPage = new OrdersPageDTO();
        ordersPage.setOrders(orders.getContent());
        ordersPage.setTotalPages(orders.getTotalPages());
        ordersPage.setOffset(orders.getPageable().getOffset());
        ordersPage.setLimit(size);
        ordersPage.setCurrentPage(orders.getPageable().getPageNumber());
        ordersPage.setTotalOrders(orders.getTotalElements());
        return ResponseEntity.ok().body(ordersPage);
    }
}
