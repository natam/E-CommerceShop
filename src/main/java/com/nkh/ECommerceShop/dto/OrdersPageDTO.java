package com.nkh.ECommerceShop.dto;

import com.nkh.ECommerceShop.model.order.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersPageDTO {
    private List<Order> orders;
    private int currentPage;
    private long offset;
    private int limit;
    private int totalPages;
    private long totalProducts;
}
