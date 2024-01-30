package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.NotEnoughProductQuantityException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.CartsRepository;
import com.nkh.ECommerceShop.security.service.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartsService {
    private final CartsRepository cartsRepository;
    private final ProductsService productsService;

    @Autowired
    public CartsService(CartsRepository cartsRepository, ProductsService productsService) {
        this.cartsRepository = cartsRepository;
        this.productsService = productsService;
    }

    public Cart getMyCart() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cartsRepository.findByUserId(userDetails.getId()).get();
    }

    @Transactional
    public void addProductToCart(long productId, int quantity) {
        Product product = productsService.getById(productId);
        if (productsService.checkProductStock(productId, quantity)) {
            Cart myCart = getMyCart();
            Optional<CartProduct> foundCartProduct = myCart
                    .getCartProducts()
                    .stream()
                    .filter(cartProducts -> cartProducts.getProduct()
                            .getId() == productId)
                    .findFirst();
            if (foundCartProduct.isPresent()) {
                foundCartProduct.get()
                        .setProductQuantity(foundCartProduct.get()
                                .getProductQuantity() + quantity);
            } else {
                myCart.getCartProducts().add(new CartProduct(myCart.getId(), product, quantity));
            }
        } else {
            throw new NotEnoughProductQuantityException(product.getStock());
        }
    }
}
