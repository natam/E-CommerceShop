package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartsProductsRepository extends JpaRepository<CartProduct, Long> {
}
