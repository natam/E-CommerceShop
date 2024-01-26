package com.nkh.ECommerceShop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank(message = "Product name must not be empty")
    private String name;
    private String description;
    @DecimalMin(value = "0", inclusive = false, message = "Product price must be greater than 0")
    private double price;
    @Min(value = 0, message = "Product stock must be greater or equal to 0")
    private int stock;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Product(String name, String description, double price, int stock){
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }
}
