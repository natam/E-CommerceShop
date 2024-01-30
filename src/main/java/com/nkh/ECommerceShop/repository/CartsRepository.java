package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartsRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(long userId);
}
