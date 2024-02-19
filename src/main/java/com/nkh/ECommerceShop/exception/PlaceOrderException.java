package com.nkh.ECommerceShop.exception;

import java.io.Serial;

public class PlaceOrderException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public PlaceOrderException(String msg) {
        super(msg);
    }
}
