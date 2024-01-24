package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.AlreadyExistsException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.model.RefreshToken;
import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.repository.ProductsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {
    @Mock
    ProductsRepository productsRepository;

    @InjectMocks
    ProductsService productsService;

    @Test
    void givenValidProduct_ReturnCreatedProductOnCreate() {
        Product product = new Product("product", "testing", 5.50, 5);
        Mockito.when(productsRepository.save(product)).thenReturn(product);
        Product addedProduct = productsService.createProduct(product);
        assertEquals(product, addedProduct);
    }

    @Test
    void givenProductWithNameAndPriceExistsInDB_ThrowExceptionOnCreate() {
        Product product = new Product("product", "testing", 5.50, 5);
        Mockito.when(productsRepository.existsByNameAndPrice(product.getName(), product.getPrice()))
                .thenReturn(true);
        Exception exception = assertThrows(AlreadyExistsException.class,
                () -> productsService.createProduct(product));
        assertEquals(exception.getMessage(),
                String.format("Product with name %s and price %f already exists", product.getName(), product.getPrice()));
    }

    @Test
    void givenProductNotExistInDB_ThrowExceptionOnFind() {
        long productId = 1L;
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> productsService.getById(productId));
        assertEquals(exception.getMessage(),
                String.format(String.format("Product with id %d is not found", productId)));
    }

    @Test
    void givenProductExistInDB_ReturnProductOnFind() {
        long productId = 1L;
        Product product = new Product("product", "testing", 5.50, 5);
        product.setId(productId);
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.of(product));
        Product foundProduct = productsService.getById(productId);
        assertEquals(product, foundProduct);
    }

    @Test
    void givenProductExistInDB_ReturnUpdatedProduct() {
        long productId = 1L;
        Product product1 = new Product("product1", "testing", 5.50, 5);
        product1.setId(productId);
        Product product2 = new Product("product1", "edited", 5.50, 20);
        product2.setId(productId);
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.of(product1));
        Mockito.when(productsRepository.save(product2)).thenReturn(product2);
        Product upddatedProduct = productsService.updateProduct(productId, product2);
        assertEquals(product2, upddatedProduct);
    }

    @Test
    void givenProductNotExistInDB_ThrowExceptionOnUpdate() {
        long productId = 1L;
        Product product = new Product("product1", "edited", 5.50, 20);
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> productsService.updateProduct(productId, product));
        assertEquals(exception.getMessage(),
                String.format(String.format("Product with id %d is not found", productId)));
    }

    @Test
    void givenProductStockGreaterThenRequestedAmount_ReturnTrue() {
        long productId = 1L;
        Product product = new Product("product", "edited", 5.50, 10);
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.of(product));
        assertTrue(productsService.checkProductStock(productId, 7));
    }

    @Test
    void givenProductStockEqualsToRequestedAmount_ReturnTrue() {
        long productId = 1L;
        Product product = new Product("product", "edited", 5.50, 10);
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.of(product));
        assertTrue(productsService.checkProductStock(productId, 10));
    }

    @Test
    void givenProductStockLessThenRequestedAmount_ReturnFalse() {
        long productId = 1L;
        Product product = new Product("product", "edited", 5.50, 10);
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.of(product));
        assertFalse(productsService.checkProductStock(productId, 11));
    }

    @Test
    void givenProductNotExistInDB_TrowExceptionOnDelete() {
        long productId = 1L;
        Mockito.when(productsRepository.findById(productId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> productsService.deleteProduct(productId));
        assertEquals(exception.getMessage(),
                String.format(String.format("Product with id %d is not found", productId)));
    }
}