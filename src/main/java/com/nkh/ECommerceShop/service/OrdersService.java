package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.NotEnoughProductQuantityException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import com.nkh.ECommerceShop.repository.OrderStatusesRepository;
import com.nkh.ECommerceShop.repository.OrdersProductsRepository;
import com.nkh.ECommerceShop.repository.OrdersRepository;
import com.nkh.ECommerceShop.repository.OrdersStatusesHistoryRepository;
import com.nkh.ECommerceShop.security.service.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersService {
    private final CartsService cartService;
    private final OrdersRepository ordersRepository;
    private final ProductsService productsService;
    private final OrderStatusesRepository statusesRepository;
    private final UserDetailsServiceImpl usersService;

    @Autowired
    public OrdersService(CartsService cartService, OrdersRepository ordersRepository, ProductsService productsService, OrderStatusesRepository statusesRepository, OrdersProductsRepository ordersProductsRepository, OrdersStatusesHistoryRepository statusesHistoryRepository, UserDetailsServiceImpl userDetailsService) {
        this.cartService = cartService;
        this.ordersRepository = ordersRepository;
        this.productsService = productsService;
        this.statusesRepository = statusesRepository;
        this.usersService = userDetailsService;
    }

    public List<Order> getMyOrders(){
        return ordersRepository.findByUserId(usersService.getCurrentUserId());
    }

    @Transactional
    public Order placeOrderFromCart(){
        long currentUserId = usersService.getCurrentUserId();
        Cart cart = cartService.getMyCart();
        cart.getCartProducts().forEach(cartProduct -> {
            if (!productsService.checkProductStock(
                    cartProduct.getProduct().getId(),
                    cartProduct.getProductQuantity())) {
                throw new NotEnoughProductQuantityException(cartProduct.getProduct().getStock());
            }
        });
        Order order = new Order(currentUserId, cart.getTotalCartProductsPrice());
        order.setProducts(cart.getCartProducts());
        order.getTrackStatuses()
                .add(new OrderStatusHistory(order.getId(),
                        statusesRepository.findById(1L).get()));
        order.getProducts()
                .forEach(orderProduct -> {
                    productsService.reduceProductStockQuantity(
                            orderProduct.getProduct().getId(),
                            orderProduct.getProductQuantity());});
        cartService.cleanCart();
        return order;
    }
}
