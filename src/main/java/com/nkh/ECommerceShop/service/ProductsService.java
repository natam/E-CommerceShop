package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.AlreadyExistsException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import jakarta.transaction.Transactional;
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

    public Product getById(long id){
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format("Product with id %d is not found", id)));
    }

    @Transactional
    public Product updateProduct(long id, Product product){
        Product productToUpdate = getById(id);
        product.setId(productToUpdate.getId());
        return repository.save(product);
    }

    public boolean checkProductStock(long productId, int requestedAmount){
        Product requestedProduct = getById(productId);
        return requestedProduct.getStock()>=requestedAmount;
    }

    @Transactional
    public void deleteProduct(long id){
        repository.delete(getById(id));
    }
}
