package com.nkh.ECommerceShop.dto;

import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderProduct;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class OrderDTO {
    private long id;
    private long userId;
    private OrderStatusHistory lastStatus;
    private Set<OrderProduct> products = new HashSet<OrderProduct>();
    private double totalOrderSum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderDTO(Order order){
        id = order.getId();
        userId = order.getUserId();
        lastStatus = Collections.max(order.getTrackStatuses(), Comparator.comparing(OrderStatusHistory::getCreatedAt));
        totalOrderSum = order.getTotalOrderSum();
        products = order.getProducts();
        createdAt = order.getCreatedAt();
        updatedAt = order.getUpdatedAt();
    }
}
