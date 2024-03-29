package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersStatusesHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    OrderStatusHistory findFirstByOrderId(long orderId);
    OrderStatusHistory findByOrderIdAndStatusStatusName(long orderId, String statusName);
}
