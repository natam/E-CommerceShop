package com.nkh.ECommerceShop.model.order;

import com.nkh.ECommerceShop.model.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orderProducts")
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId")
    private Product product;
    private int productQuantity;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public OrderProduct(long orderId, Product product, int productQuantity) {
        this.orderId = orderId;
        this.product = product;
        this.productQuantity = productQuantity;
    }
}
