package com.nkh.ECommerceShop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Set<CartProducts> cartProducts = new HashSet<CartProducts>();;
    private double totalCartProductsPrice;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public Cart(long userId, Set<CartProducts> cartProducts){
        this.userId = userId;
        this.cartProducts = cartProducts;
        if(cartProducts.isEmpty()){
            totalCartProductsPrice = 0;
        }else {
            totalCartProductsPrice = cartProducts
                    .stream()
                    .mapToDouble(cartProducts1 -> cartProducts1.getProduct().getPrice() * cartProducts1.getProductQuantity())
                    .reduce(0.00, Double::sum);
        }
    }
}
