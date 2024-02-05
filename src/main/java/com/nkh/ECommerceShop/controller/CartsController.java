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

    @GetMapping("/mycart")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Cart> getMyCart(){
        return ResponseEntity.ok(cartsService.getMyCart());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> deleteCart(@PathVariable("id") long id){
        cartsService.deleteProductFromCart(id);
        String message = String.format("Product with id %d was removed from cart", id);
        return ResponseEntity.ok().body(new MessageResponseDTO(message));
    }

    @DeleteMapping("mycart/products/{id}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponseDTO> deleteProductFromMyCart(@PathVariable("id") long id){
        cartsService.deleteProductFromCart(id);
        String message = String.format("Product with id %d was removed from cart", id);
        return ResponseEntity.ok().body(new MessageResponseDTO(message));
    }

    @PostMapping("mycart/products/reduce")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponseDTO> reduceProductAmountInMyCart(@RequestParam("productId") long id){
        cartsService.reduceProductQuantityInCart(id);
        String message = String.format("Product with id %d was reduced", id);
        return ResponseEntity.ok().body(new MessageResponseDTO(message));
    }

    @PostMapping("/mycart/clear")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MessageResponseDTO> clearMyCart(){
        cartsService.cleanCart();
        return ResponseEntity.ok(new MessageResponseDTO("Cart was cleaned"));
    }
}
