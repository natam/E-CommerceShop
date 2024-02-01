package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.dto.MessageResponseDTO;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.service.CartsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CartsController {
    private final CartsService cartsService;
    @Autowired
    public CartsController(CartsService cartsService){
        this.cartsService = cartsService;
    }

    @PostMapping("/mycart/products/add")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponseDTO> addProductToMyCart(@RequestParam("productId") long productId,
                                                                 @RequestParam("quantity") int quantity){
        cartsService.addProductToCart(productId, quantity);
        return ResponseEntity.ok().body(new MessageResponseDTO("Product was added to your cart"));
    }
}
