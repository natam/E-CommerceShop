package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.order.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersProductsRepository extends JpaRepository<OrderProduct, Long> {

}
