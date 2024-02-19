package com.nkh.ECommerceShop.dto.auth;

import com.nkh.ECommerceShop.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccessTokenDTO {
    private String user;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
    private String expiration;
}
