package com.nkh.ECommerceShop.dto;

import com.nkh.ECommerceShop.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsPageDTO {
    private List<Product> products;
    private int currentPage;
    private long offset;
    private int limit;
    private int totalPages;
    private long totalProducts;
}
