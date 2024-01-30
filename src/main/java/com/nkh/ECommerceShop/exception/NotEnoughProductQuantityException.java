package com.nkh.ECommerceShop.exception;

import java.io.Serial;

public class NotEnoughProductQuantityException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public NotEnoughProductQuantityException(int availableQuantity) {
        super(String.format("Not enough product quantity in stock. Available quantity is %d", availableQuantity));
    }
}
