package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.NotEnoughProductQuantityException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.CartsProductsRepository;
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
    private final CartsProductsRepository cartsProductsRepository;
    private final ProductsService productsService;

    @Autowired
    public CartsService(CartsRepository cartsRepository, CartsProductsRepository cartsProductsRepository, ProductsService productsService) {
        this.cartsRepository = cartsRepository;
        this.cartsProductsRepository = cartsProductsRepository;
        this.productsService = productsService;
    }

    public Cart getMyCart() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cartsRepository.findByUserId(userDetails.getId()).get();
    }

    public Cart createCart(long userId) {
        return cartsRepository.save(new Cart(userId));
    }

    @Transactional
    public void cleanCart() {
        cartsProductsRepository.deleteAllByCartId(getMyCart().getId());
        getMyCart().setTotalCartProductsPrice(0);
    }

    @Transactional
    public void addProductToCart(long productId, int quantity) {
        Product product = productsService.getById(productId);
        if (productsService.checkProductStock(productId, quantity)) {
            Cart myCart = getMyCart();
            //check if product presents in cart. If yes - increase its quantity, otherwise - add it to cart
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
            myCart.setTotalCartProductsPrice(myCart.getTotalCartProductsPrice() + product.getPrice() * quantity);
        } else {
            throw new NotEnoughProductQuantityException(product.getStock());
        }
    }

    @Transactional
    public void reduceProductQuantityInCart(long productId) {
        Product product = productsService.getById(productId);
        Cart myCart = getMyCart();
        //check if product presents in cart
        Optional<CartProduct> foundCartProduct = myCart
                .getCartProducts()
                .stream()
                .filter(cartProducts -> cartProducts.getProduct()
                        .getId() == productId)
                .findFirst();
        if (foundCartProduct.isPresent()) {
            //if product from cart if its quantity is 1 otherwise decrease its quantity in cart on 1
            if(foundCartProduct.get().getProductQuantity()==1){
                cartsProductsRepository.deleteAllByCartId(myCart.getId());
            }else {
                foundCartProduct.get()
                        .setProductQuantity(foundCartProduct.get()
                                .getProductQuantity() - 1);
            }
            myCart.setTotalCartProductsPrice(myCart.getTotalCartProductsPrice() - product.getPrice());
        } else {
            throw new ResourceNotFoundException(String.format("Product with id %d is not found in cart", productId));
        }
    }

    @Transactional
    public void deleteProductFromCart(long productId){
        Product product = productsService.getById(productId);
        Cart myCart = getMyCart();
        //check if product is in cart
        Optional<CartProduct> foundCartProduct = myCart
                .getCartProducts()
                .stream()
                .filter(cartProducts -> cartProducts.getProduct()
                        .getId() == productId)
                .findFirst();
        if (foundCartProduct.isPresent()) {
            double removedProductPrice = foundCartProduct.get().getProductQuantity()*product.getPrice();
            myCart.setTotalCartProductsPrice(myCart.getTotalCartProductsPrice() - removedProductPrice);
            cartsProductsRepository.deleteAllByCartId(myCart.getId());
        } else {
            throw new ResourceNotFoundException(String.format("Product with id %d is not found in cart", productId));
        }
    }
}