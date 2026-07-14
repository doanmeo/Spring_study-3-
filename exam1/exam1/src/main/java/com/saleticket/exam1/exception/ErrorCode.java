package com.saleticket.exam1.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_EXISTED(1001, "Người dùng đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "Không tìm thấy người dùng!", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(1003, "Sai tài khoản hoặc mật khẩu!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Bạn chưa đăng nhập hoặc token không hợp lệ!", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1005, "Bạn không có quyền truy cập tài nguyên này!", HttpStatus.FORBIDDEN),
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định!", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
