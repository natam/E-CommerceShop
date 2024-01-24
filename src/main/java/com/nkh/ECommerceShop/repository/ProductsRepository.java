package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.Product;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Long> {
    boolean existsByNameAndPrice(String name, double price);
}