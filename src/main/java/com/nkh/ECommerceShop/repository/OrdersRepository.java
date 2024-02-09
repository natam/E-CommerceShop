package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.search.OrderSpecification;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserId(long userId);
    Optional<Order> findByIdAndUserId(long id, long userId);
    Page<Order> findAllByUserId(long userId, Pageable pageable);
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
}
