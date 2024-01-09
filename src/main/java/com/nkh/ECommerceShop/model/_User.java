package com.nkh.ECommerceShop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class _User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userId;
    @NotBlank
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private ERole role;

    public _User(String name, String email, String password, ERole role){
        this.name=name;
        this.email=email;
        this.password=password;
        this.role = role;
    }
}
