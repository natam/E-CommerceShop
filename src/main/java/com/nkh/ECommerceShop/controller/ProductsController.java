package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.dto.MessageResponseDTO;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import com.nkh.ECommerceShop.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {
    private final ProductsService productsService;
    private final ProductsRepository productsRepository;
    @Autowired
    public ProductsController(ProductsService productsService, ProductsRepository productsRepository){
        this.productsRepository = productsRepository;
        this.productsService = productsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(@RequestBody Product product){
        Product addedProduct = productsService.createProduct(product);
        return ResponseEntity.ok().body(addedProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") long id,
                                                 @RequestBody Product product){
        Product updatedProduct = productsService.updateProduct(id, product);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Product> getProduct(@PathVariable("id") long id){
        Product foundProduct = productsService.getById(id);
        return ResponseEntity.ok().body(foundProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> deleteProduct(@PathVariable("id") long id){
        productsService.deleteProduct(id);
        return ResponseEntity.ok().body(new MessageResponseDTO(String.format("Product with id %d was deleted", id)));
    }
}
