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
    USER_EXISTED(1001, "Người dùng đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "Chưa xác thực danh tính (vui lòng đăng nhập)", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(1005, "Tài khoản của bạn đã bị khóa, vui lòng liên hệ Admin", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1006, "Số điện thoại hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
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