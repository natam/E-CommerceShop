package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.AlreadyExistsException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import com.nkh.ECommerceShop.search.OrderSpecification;
import com.nkh.ECommerceShop.search.ProductSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public boolean deleteProduct(long id){
        repository.delete(getById(id));
        return true;
    }

    public Page<Product> getAllProducts(int page, int size, String productName, double priceStartRange, double priceEndRange, int productQuantity){
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = ProductSpecification.builder()
                .name(productName)
                .priceStart(priceStartRange)
                .priceEnd(priceEndRange)
                .stockAvailability(productQuantity)
                .build();
        Page<Product> products = repository.findAll(spec, pageable);
        if(products.isEmpty()){
            throw new ResourceNotFoundException(
                    String.format("Page %d not found. Products has %d pages",
                            products.getPageable().getPageNumber(), products.getTotalPages()));
        }
        return products;
    }

    public void reduceProductStockQuantity(long productId, int reduceAmount){
        Product product = getById(productId);
        product.setStock(product.getStock()-reduceAmount);
        updateProduct(productId, product);
    }
}
