package com.nkh.ECommerceShop.dto.auth;

import lombok.Data;

@Data
public class TokenRefreshRequestDTO {
    private String refreshToken;
}
