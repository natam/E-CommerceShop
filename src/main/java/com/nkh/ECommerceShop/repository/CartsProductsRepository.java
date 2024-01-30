package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.CartProducts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartsProductsRepository extends JpaRepository<CartProducts, Long> {
}
