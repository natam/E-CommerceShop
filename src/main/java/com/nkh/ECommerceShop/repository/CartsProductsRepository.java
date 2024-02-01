package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartsProductsRepository extends JpaRepository<CartProduct, Long> {
    void deleteAllByCartId(long cartId);
    void deleteByCartIdAndProduct(long cartId, Product product);
}
