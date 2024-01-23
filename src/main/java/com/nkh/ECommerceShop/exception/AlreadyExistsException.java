package com.nkh.ECommerceShop.exception;

import java.io.Serial;

public class AlreadyExistsException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public AlreadyExistsException(String msg) {
        super(msg);
    }
}
