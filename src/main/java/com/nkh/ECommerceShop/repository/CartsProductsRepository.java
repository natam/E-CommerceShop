package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CartsProductsRepository extends JpaRepository<CartProduct, Long> {
    @Modifying
    @Transactional
    @Query(value="delete from cartProducts where cartId=?1", nativeQuery = true)
    void deleteAllProductsByCartId(long cartId);
    @Modifying
    @Transactional
    @Query(value="delete from cartProducts where cartId=?1 and productId=?2", nativeQuery = true)
    void deleteByCartIdAndProduct(long cartId, long productId);
}
