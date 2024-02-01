package com.nkh.ECommerceShop.service;

import com.nkh.ECommerceShop.exception.NotEnoughProductQuantityException;
import com.nkh.ECommerceShop.exception.ResourceNotFoundException;
import com.nkh.ECommerceShop.model.Cart;
import com.nkh.ECommerceShop.model.CartProduct;
import com.nkh.ECommerceShop.model.Product;
import com.nkh.ECommerceShop.repository.CartsProductsRepository;
import com.nkh.ECommerceShop.repository.CartsRepository;
import com.nkh.ECommerceShop.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class CartsServiceTest {
    @Mock
    private CartsRepository cartsRepository;
    @Mock
    private CartsProductsRepository cartsProductsRepository;
    @Mock
    private ProductsService productsService;
    @InjectMocks
    private CartsService cartsService;
    @Mock
    AuthenticationManager authenticationManager;
    private final long userId = 1L;
    Cart userCart;

    @Test
    void givenAddProductWithNotEnoughAmountInStock_ThrowException(){
        long productId = 1;
        Product product1 = new Product("product1", "testing", 10.50, 5);
        product1.setId(productId);
        String errorMessage = String.format("Not enough product quantity in stock. Available quantity is %d", product1.getStock());
        Mockito.when(productsService.getById(productId)).thenReturn(product1);
        Mockito.when(productsService.checkProductStock(productId,6)).thenReturn(false);
        Exception exception = assertThrows(NotEnoughProductQuantityException.class, () -> cartsService.addProductToCart(productId, 6));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Nested
    public class requiresBeforeBlock {
        @BeforeEach
        public void setup() {
            UserDetailsImpl userDetails = new UserDetailsImpl(userId, "test@nkh.com", "test1234", List.of(new SimpleGrantedAuthority("USER")));
            Authentication authentication = Mockito.mock(Authentication.class);
            SecurityContext securityContext = Mockito.mock(SecurityContext.class);
            Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);
            Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
            userCart = new Cart(userId);
            userCart.setId(2);
            userCart.setCartProducts(new HashSet<>());
        }

        @Test
        void givenValidUserRequestsHisCart_ReturnCart() {
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            Cart foundCart = cartsService.getMyCart();
            assertEquals(userCart, foundCart);
        }

        @Test
        void givenClearNotEmptyCart_CartProductsRemovedAndPriceSetTo0() {
            Product product1 = new Product("product1", "testing", 10.50, 5);
            CartProduct cartProduct = new CartProduct(userCart.getId(), product1, 3);
            userCart.getCartProducts().add(cartProduct);
            userCart.setTotalCartProductsPrice(product1.getPrice() * 3);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            Mockito.doAnswer(i -> {
                userCart.getCartProducts().remove(cartProduct);
                return null;
            }).when(cartsProductsRepository).deleteAllByCartId(userCart.getId());
            cartsService.cleanCart();
            assertEquals(0, userCart.getCartProducts().size());
            assertEquals(0, userCart.getTotalCartProductsPrice());
        }

        @Test
        void givenClearNotExistingCart_ThrowException() {
            String errorMessage = String.format("Cart for userId %d", userId);
            Mockito.when(cartsRepository.findByUserId(userId)).thenThrow(new ResourceNotFoundException(errorMessage));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> cartsService.cleanCart());
            assertEquals(errorMessage, exception.getMessage());
        }

        @Test
        void givenAddProductToEmptyCart_TotalCartPriceIncreasedAndProductAdded() {
            long productId = 1;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            product1.setId(productId);
            Mockito.when(productsService.getById(productId)).thenReturn(product1);
            Mockito.when(productsService.checkProductStock(productId, 3)).thenReturn(true);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            cartsService.addProductToCart(productId, 3);
            assertEquals(1, userCart.getCartProducts().size());
            assertEquals(product1.getPrice() * 3, userCart.getTotalCartProductsPrice());
            assertTrue(userCart.getCartProducts().stream().anyMatch(cartItem -> cartItem.getProduct().equals(product1)));
            int productInCart = userCart.getCartProducts()
                    .stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product1))
                    .findFirst()
                    .get()
                    .getProductQuantity();
            assertEquals(3, productInCart);
        }

        @Test
        void givenAddProductToCartWithAnotherProduct_TotalCartPriceIncreasedAndProductAdded() {
            long product1Id = 1;
            long product2Id = 2;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            Product product2 = new Product("product2", "testing", 4.50, 15);
            product1.setId(product1Id);
            product2.setId(product2Id);
            CartProduct cartProduct2 = new CartProduct(userCart.getId(), product2, 5);
            userCart.setTotalCartProductsPrice(product2.getPrice() * 5);
            userCart.getCartProducts().add(cartProduct2);
            Mockito.when(productsService.getById(product1Id)).thenReturn(product1);
            Mockito.when(productsService.checkProductStock(product1Id, 3)).thenReturn(true);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            cartsService.addProductToCart(product1Id, 3);
            assertEquals(2, userCart.getCartProducts().size());
            assertEquals(product1.getPrice() * 3 + product2.getPrice() * 5, userCart.getTotalCartProductsPrice());
            assertTrue(userCart.getCartProducts().stream().anyMatch(cartItem -> cartItem.getProduct().equals(product1)));
            int productInCart = userCart.getCartProducts()
                    .stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product1))
                    .findFirst()
                    .get()
                    .getProductQuantity();
            assertEquals(3, productInCart);
        }

        @Test
        void givenAddProductThatPresentsInCart_TotalCartPriceIncreasedAndProductQuantityIncreased() {
            long productId = 1;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            product1.setId(productId);
            CartProduct cartProduct = new CartProduct(userCart.getId(), product1, 1);
            userCart.setTotalCartProductsPrice(product1.getPrice());
            userCart.getCartProducts().add(cartProduct);
            Mockito.when(productsService.getById(productId)).thenReturn(product1);
            Mockito.when(productsService.checkProductStock(productId, 3)).thenReturn(true);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            cartsService.addProductToCart(productId, 3);
            assertEquals(1, userCart.getCartProducts().size());
            assertEquals(product1.getPrice() * 4, userCart.getTotalCartProductsPrice());
            assertTrue(userCart.getCartProducts().stream().anyMatch(cartItem -> cartItem.getProduct().equals(product1)));
            int productInCart = userCart.getCartProducts()
                    .stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product1))
                    .findFirst()
                    .get()
                    .getProductQuantity();
            assertEquals(4, productInCart);
        }

        @Test
        void givenReduceProductThatPresentsInCart_TotalCartPriceDecreasedAndProductQuantityDecreased() {
            long productId = 1;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            product1.setId(productId);
            CartProduct cartProduct = new CartProduct(userCart.getId(), product1,2);
            userCart.setTotalCartProductsPrice(product1.getPrice()*2);
            userCart.getCartProducts().add(cartProduct);
            Mockito.when(productsService.getById(productId)).thenReturn(product1);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            cartsService.reduceProductQuantityInCart(productId);
            assertEquals(1, userCart.getCartProducts().size());
            assertEquals(product1.getPrice(), userCart.getTotalCartProductsPrice());
            assertTrue(userCart.getCartProducts().stream().anyMatch(cartItem -> cartItem.getProduct().equals(product1)));
            int productInCart = userCart.getCartProducts()
                    .stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product1))
                    .findFirst()
                    .get()
                    .getProductQuantity();
            assertEquals(1, productInCart);
        }

        @Test
        void givenReduceProductWithQuantityOneInCart_TotalCartPriceDecreasedAndProductRemoved() {
            long productId = 1;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            product1.setId(productId);
            CartProduct cartProduct = new CartProduct(userCart.getId(), product1, 1);
            userCart.setTotalCartProductsPrice(product1.getPrice());
            userCart.getCartProducts().add(cartProduct);
            Mockito.when(productsService.getById(productId)).thenReturn(product1);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            Mockito.doAnswer(i -> {
                userCart.getCartProducts().remove(cartProduct);
                return null;
            }).when(cartsProductsRepository).deleteByCartIdAndProduct(userCart.getId(), product1);
            cartsService.reduceProductQuantityInCart(productId);
            assertEquals(0, userCart.getCartProducts().size());
            assertEquals(0, userCart.getTotalCartProductsPrice());
        }

        @Test
        void givenReduceProductThatNotInCart_ThrowException(){
            long productId = 1;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            product1.setId(productId);
            String errorMessage = String.format("Product with id %d is not found in cart", productId);
            Mockito.when(productsService.getById(productId)).thenReturn(product1);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> cartsService.reduceProductQuantityInCart(productId));
            assertEquals(errorMessage, exception.getMessage());
        }

        @Test
        void givenRemoveProductFromCartWithTwoProducts_TotalCartPriceIncreasedAndOneProductRemoved() {
            long product1Id = 1;
            long product2Id = 2;
            Product product1 = new Product("product1", "testing", 10.50, 5);
            Product product2 = new Product("product2", "testing", 4.50, 15);
            product1.setId(product1Id);
            product2.setId(product2Id);
            CartProduct cartProduct1 = new CartProduct(userCart.getId(), product1, 3);
            CartProduct cartProduct2 = new CartProduct(userCart.getId(), product2, 5);
            cartProduct2.setId(1);
            userCart.setTotalCartProductsPrice(product2.getPrice() * 5 + product1.getPrice()*3);
            userCart.getCartProducts().add(cartProduct2);
            userCart.getCartProducts().add(cartProduct1);
            Mockito.when(productsService.getById(product2Id)).thenReturn(product2);
            Mockito.doAnswer(i -> {
                userCart.getCartProducts().remove(cartProduct2);
                return null;
            }).when(cartsProductsRepository).deleteByCartIdAndProduct(userCart.getId(), product2);
            Mockito.when(cartsRepository.findByUserId(userId)).thenReturn(Optional.of(userCart));
            cartsService.deleteProductFromCart(product2Id);
            assertEquals(1, userCart.getCartProducts().size());
            assertEquals(product1.getPrice() * 3, userCart.getTotalCartProductsPrice());
            assertTrue(userCart.getCartProducts().stream().anyMatch(cartItem -> cartItem.getProduct().equals(product1)));
            int productInCart = userCart.getCartProducts()
                    .stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product1))
                    .findFirst()
                    .get()
                    .getProductQuantity();
            assertEquals(3, productInCart);
        }
    }
}