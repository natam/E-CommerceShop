package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusesRepository extends JpaRepository<OrderStatus, Long> {
}
