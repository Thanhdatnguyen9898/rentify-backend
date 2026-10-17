package com.rentify.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import lombok.Getter;

@Getter
public enum ErrorCode {
    // 1. Nhóm lỗi hệ thống chung
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "Khóa thông điệp lỗi không hợp lệ", HttpStatus.BAD_REQUEST),

    // 2. Nhóm lỗi Xác thực & Người dùng (1xxx)
    PHONE_EXISTED(1001, "Số điện thoại đã được đăng ký", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "Chưa xác thực danh tính (vui lòng đăng nhập)", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(1005, "Tài khoản của bạn đã bị khóa, vui lòng liên hệ Admin", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1006, "Thông tin đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(1007, "Tên đăng nhập (username) đã tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1008, "Email đã được sử dụng bởi tài khoản khác", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_INVALID(1009, "Refresh Token không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1010, "Mã OTP không chính xác hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_COOLDOWN(1011, "Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới", HttpStatus.TOO_MANY_REQUESTS),
    ;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}