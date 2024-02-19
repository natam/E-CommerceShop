package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.exception.NotValidInputException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.search.OrderSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
class OrdersRepositoryTest {
    @Autowired
    private OrdersRepository ordersRepository;

    @Test
    void givenFindAllAndFilterByStartDate_ReturnCorrectOrders() {
        Order order1 = ordersRepository.findById(1L).get();
        Order order2 = ordersRepository.findById(2L).get();
        Order order3 = ordersRepository.findById(3L).get();
        order1.setCreatedAt(LocalDateTime.of(2024, Month.FEBRUARY, 2, 10, 30));
        Specification<Order> spec = OrderSpecification.builder()
                .startDate(LocalDate.now().toString())
                .endDate(null)
                .orderSum(null)
                .userId((null))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Page<Order> result = ordersRepository.findAll(spec, pageable);
        assertEquals(List.of(order2, order3), result.getContent());
    }

    @Test
    void givenNoFiltersProvided_ReturnAllOrders() {
        Specification<Order> spec = OrderSpecification.builder()
                .startDate(null)
                .endDate(null)
                .orderSum(null)
                .userId((null))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Page<Order> result = ordersRepository.findAll(spec, pageable);
        assertEquals(ordersRepository.findAll(pageable), result);
    }

    @Test
    void givenFilterByUserId_ReturnCorrectOrders() {
        Specification<Order> spec = OrderSpecification.builder()
                .startDate(null)
                .endDate(null)
                .orderSum(null)
                .userId((1L))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        List<Order> expectedResult = List.of(ordersRepository.findById(1L).get(), ordersRepository.findById(3L).get());
        Page<Order> result = ordersRepository.findAll(spec, pageable);
        assertEquals(expectedResult, result.getContent());
    }

    @Test
    void givenFilterBySum_ReturnCorrectOrders() {
        Specification<Order> spec = OrderSpecification.builder()
                .startDate(null)
                .endDate(null)
                .orderSum(20)
                .userId((null))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Page<Order> result = ordersRepository.findAll(spec, pageable);
        assertEquals(List.of(ordersRepository.findById(1L).get()), result.getContent());
    }

    @Test
    void givenFilterByUserIdAndStartDate_ReturnCorrectOrders() {
        Order order3 = ordersRepository.findById(3L).get();
        order3.setCreatedAt(LocalDateTime.of(2024, Month.FEBRUARY, 2, 10, 30));
        Specification<Order> spec = OrderSpecification.builder()
                .startDate(LocalDate.now().toString())
                .endDate(null)
                .orderSum(null)
                .userId((1L))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Page<Order> result = ordersRepository.findAll(spec, pageable);
        assertEquals(List.of(ordersRepository.findById(1L).get()), result.getContent());
    }

    @Test
    void givenFilterByUserIdWithoutOrders_ReturnEmptyList() {
        Specification<Order> spec = OrderSpecification.builder()
                .startDate(null)
                .endDate(null)
                .orderSum(null)
                .userId((3L))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Page<Order> result = ordersRepository.findAll(spec,pageable);
        assertEquals(Collections.emptyList(), result.getContent());
    }

    @Test
    void givenFilterByStartAndEndDate_ReturnCorrectOrders() {
        Order order1 = ordersRepository.findById(1L).get();
        order1.setCreatedAt(LocalDate.of(2024, 2,2).atStartOfDay());
        Order order3 = ordersRepository.findById(3L).get();
        order3.setCreatedAt(LocalDate.of(2024,2,6).atStartOfDay());
        Specification<Order> spec = OrderSpecification.builder()
                .startDate("01.02.2024")
                .endDate("10.02.2024")
                .orderSum(null)
                .userId((null))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Page<Order> result = ordersRepository.findAll(spec, pageable);
        assertEquals(List.of(order1,order3), result.getContent());
    }

    @Test
    void givenFilterByNotValidStartDate_ThrowException() {
        Order order1 = ordersRepository.findById(1L).get();
        order1.setCreatedAt(LocalDate.of(2024, 2,2).atStartOfDay());
        Order order3 = ordersRepository.findById(3L).get();
        order3.setCreatedAt(LocalDate.of(2024,2,6).atStartOfDay());
        Specification<Order> spec = OrderSpecification.builder()
                .startDate("01.02.rrrr")
                .endDate(null)
                .orderSum(null)
                .userId((null))
                .build();
        Pageable pageable = PageRequest.of(0,5);
        Exception exception = assertThrows(NotValidInputException.class, () -> ordersRepository.findAll(spec, pageable));
        String errorMessage = "Not correct date input 01.02.rrrr";
        assertEquals(errorMessage, exception.getMessage());
    }
}