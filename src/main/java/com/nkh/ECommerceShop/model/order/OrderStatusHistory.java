package com.nkh.ECommerceShop.model.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "orderStatusHistory")
@Data
@NoArgsConstructor
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusId")
    private OrderStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
