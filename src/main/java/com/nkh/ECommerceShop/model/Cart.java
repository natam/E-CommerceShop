package com.nkh.ECommerceShop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long userId;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cartId")
    private Set<CartProduct> cartProducts = new HashSet<CartProduct>();;
    private double totalCartProductsPrice;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public Cart(long userId, Set<CartProduct> cartProducts){
        this.userId = userId;
        this.cartProducts = cartProducts;
        if(cartProducts.isEmpty()){
            totalCartProductsPrice = 0;
        }else {
            totalCartProductsPrice = cartProducts
                    .stream()
                    .mapToDouble(cartProduct1 -> cartProduct1.getProduct().getPrice() * cartProduct1.getProductQuantity())
                    .reduce(0.00, Double::sum);
        }
    }
}
