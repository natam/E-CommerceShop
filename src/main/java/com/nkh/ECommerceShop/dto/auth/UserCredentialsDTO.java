package com.nkh.ECommerceShop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
public class UserCredentialsDTO {
    @Email(message = "Should be provided valid email")
    @NotBlank(message = "Email can not be empty")
    private String email;
    @NotBlank(message = "Password can not be empty")
    private String password;
}
