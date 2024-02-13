package com.nkh.ECommerceShop.exception;

import java.io.Serial;

public class NotValidInputException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public NotValidInputException(String msg) {
        super(msg);
    }
}
