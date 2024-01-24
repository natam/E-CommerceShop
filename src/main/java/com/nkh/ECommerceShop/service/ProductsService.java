package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.AlreadyExistsException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductsService {
    private final ProductsRepository repository;
    @Autowired
    public ProductsService(ProductsRepository repository){
        this.repository = repository;
    }

    public Product createProduct(Product product){
        if(repository.existsByNameAndPrice(product.getName(), product.getPrice())){
            throw new AlreadyExistsException(String.format("Product with name %s and price %f already exists", product.getName(), product.getPrice()));
        }
        return repository.save(product);
    }
}
