package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.PlaceOrderException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderStatus;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import com.nkh.ECommerceShop.repository.*;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersService {
    private final CartsService cartService;
    private final OrdersRepository ordersRepository;
    private final OrdersStatusesHistoryRepository statusesHistoryRepository;
    private final ProductsService productsService;
    private final OrderStatusesRepository statusesRepository;
    private final UserDetailsServiceImpl usersService;

    @Autowired
    public OrdersService(CartsService cartService, OrdersRepository ordersRepository, OrdersStatusesHistoryRepository statusesHistoryRepository, ProductsService productsService, OrderStatusesRepository statusesRepository, UserDetailsServiceImpl userDetailsService) {
        this.cartService = cartService;
        this.ordersRepository = ordersRepository;
        this.statusesHistoryRepository = statusesHistoryRepository;
        this.productsService = productsService;
        this.statusesRepository = statusesRepository;
        this.usersService = userDetailsService;
    }

    public Page<Order> getMyOrders(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = ordersRepository
                .findAllByUserId(usersService.getCurrentUserId(), pageable);
        if(orders.isEmpty()){
            throw new ResourceNotFoundException(
                    String.format("Page %d not found. Products has %d pages",
                            orders.getPageable().getPageNumber(), orders.getTotalPages()));
        }
        return orders;
    }

    public Page<Order> getAllOrders(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = ordersRepository.findAll(pageable);
        if(orders.isEmpty()){
            throw new ResourceNotFoundException(
                    String.format("Page %d not found. Products has %d pages",
                            orders.getPageable().getPageNumber(), orders.getTotalPages()));
        }
        return orders;
    }

    @Transactional
    public boolean updateOrderStatus(long orderId, long statusId){
        OrderStatus newStatus = statusesRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Status with id %d not found in DB", statusId)));
        statusesHistoryRepository.save(new OrderStatusHistory(orderId, newStatus));
        return true;
    }

    public Order getOrderById(long id){
        return ordersRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                String.format("Order with id %d not found", id))
                );
    }

    public Order getMyOrderById(long id){
        return ordersRepository.findByIdAndUserId(id, usersService.getCurrentUserId())
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                String.format("Order with id %d not found", id))
                );
    }

    @Transactional
    public Order placeOrderFromCart(){
        long currentUserId = usersService.getCurrentUserId();
        Cart cart = cartService.getMyCart();
        if(cart.getCartProducts().isEmpty()){
            throw new PlaceOrderException("Order can not be placed. Cart is empty.");
        }
        cart.getCartProducts().forEach(cartProduct -> {
            if (!productsService.checkProductStock(
                    cartProduct.getProduct().getId(),
                    cartProduct.getProductQuantity())) {
                throw new PlaceOrderException(String.format("Order can not be placed. Not enough product (id: %d) quantity in stock", cartProduct.getProduct().getId()));
            }
        });
        Order order = new Order(currentUserId);
        order = ordersRepository.save(order);
        order.setTotalOrderSum(cart.getTotalCartProductsPrice());
        order.setProducts(cart.getCartProducts());
        order.getTrackStatuses()
                .add(new OrderStatusHistory(order.getId(),
                        statusesRepository.findById(1L).get()));
        ordersRepository.save(order);
        order.getProducts()
                .forEach(orderProduct -> {
                    productsService.reduceProductStockQuantity(
                            orderProduct.getProduct().getId(),
                            orderProduct.getProductQuantity());});
        cartService.cleanCart();
        return order;
    }
}
