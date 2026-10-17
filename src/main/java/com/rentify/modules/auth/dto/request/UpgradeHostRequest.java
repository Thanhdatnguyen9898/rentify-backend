package com.rentify.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
@Getter 
@Setter 
public class UpgradeHostRequest {
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern (regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng")
    private String phoneNumber;
    
    @NotBlank 
    @Size(min = 6, max = 6, message = "Mã OTP phải đúng 6 chữ số")
    private String otpCode;
}
