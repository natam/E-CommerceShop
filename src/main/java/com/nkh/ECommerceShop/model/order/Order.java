package com.nkh.ECommerceShop.model.order;

import com.nkh.ECommerceShop.repository.OrdersRepository;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "orders")
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long userId;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusHistory> trackStatuses;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "orderId")
    private Set<OrderProduct> products;
    private double totalOrderSum;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Order(long userId){
        this.userId = userId;
        totalOrderSum = 0;
    }
}
