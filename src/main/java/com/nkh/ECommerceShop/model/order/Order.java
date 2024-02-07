package com.nkh.ECommerceShop.model.order;
import com.nkh.ECommerceShop.model.CartProduct;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
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
    private Set<OrderStatusHistory> trackStatuses = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "orderId")
    private Set<OrderProduct> products = new HashSet<OrderProduct>();
    private double totalOrderSum;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Order(long userId){
        this.userId = userId;
        totalOrderSum = 0;
    }

    public Order(long userId, double totalOrderSum){
        this.userId = userId;
        this.totalOrderSum = totalOrderSum;
    }

    public void setProducts(Set<CartProduct> cartProducts){
        cartProducts.forEach(cartProduct -> {
            products.add(new OrderProduct(id, cartProduct));
        });
    }
}
