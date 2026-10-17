package com.rentify.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
@Getter 
@Setter 
public class SendOtpRequest {
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern (regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng")
    private String phoneNumber;
}
