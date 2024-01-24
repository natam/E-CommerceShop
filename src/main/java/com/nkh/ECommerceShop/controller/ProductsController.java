package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import com.nkh.ECommerceShop.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class ProductsController {
    private final ProductsService productsService;
    private final ProductsRepository productsRepository;
    @Autowired
    public ProductsController(ProductsService productsService, ProductsRepository productsRepository){
        this.productsRepository = productsRepository;
        this.productsService = productsService;
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(@RequestBody Product product){
        Product addedProduct = productsService.createProduct(product);
        return ResponseEntity.ok().body(addedProduct);
    }
}
