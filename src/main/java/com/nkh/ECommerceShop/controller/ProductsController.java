package com.nkh.ECommerceShop.controller;

import com.nkh.ECommerceShop.dto.MessageResponseDTO;
import com.nkh.ECommerceShop.dto.ProductsPageDTO;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.service.ProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {
    private final ProductsService productsService;
    @Autowired
    public ProductsController(ProductsService productsService){
        this.productsService = productsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product){
        Product addedProduct = productsService.createProduct(product);
        return ResponseEntity.ok().body(addedProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") long id,
                                                 @Valid @RequestBody Product product){
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProductsPageDTO> getProducts(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "3") int size,
                                                       @RequestParam(value = "productName", required = false) String productName,
                                                       @RequestParam(value = "startPrice", required = false, defaultValue = "0") double startPrice,
                                                       @RequestParam(value = "endPrice", required = false, defaultValue = "0") double endPrice,
                                                       @RequestParam(value = "productQuantity", required = false, defaultValue = "0") int productQuantity){
        ProductsPageDTO productsResponse = new ProductsPageDTO();
        Page<Product> products = productsService.getAllProducts(page,size,productName,startPrice,endPrice,productQuantity);
        productsResponse.setProducts(products.getContent());
        productsResponse.setLimit(size);
        productsResponse.setTotalPages(products.getTotalPages());
        productsResponse.setTotalProducts(products.getTotalElements());
        productsResponse.setCurrentPage(products.getPageable().getPageNumber());
        productsResponse.setOffset(products.getPageable().getOffset());
        return ResponseEntity.ok().body(productsResponse);
    }
}
