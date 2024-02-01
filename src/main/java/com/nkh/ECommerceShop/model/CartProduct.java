package com.nkh.ECommerceShop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "cartProducts")
@NoArgsConstructor
public class CartProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long cartId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId")
    private Product product;
    private int productQuantity;

    public CartProduct(long cartId, Product product, int productQuantity){
        this.cartId=cartId;
        this.product = product;
        this.productQuantity = productQuantity;
    }
}
