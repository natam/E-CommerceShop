package com.nkh.ECommerceShop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequestDTO {
    @NotBlank(message = "Name can not be empty")
    @Size(min = 3, max = 50, message = "Minimum name length should be 3 letters and maximum 50")
    private String name;
    @Email(message = "Should be provided valid email")
    @NotBlank(message = "Email can not be empty")
    private String email;
    @NotBlank(message = "Password can not be empty")
    @Size(min = 6, max = 40, message = "Minimum password length should be 6 signs and maximum 50")
    private String password;
}
