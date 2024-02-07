package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.order.Order;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(long userId);
    Page<Order> findAllByUserId(long userId, Pageable pageable);
    Page<Order> findAll(Pageable pageable);
}
