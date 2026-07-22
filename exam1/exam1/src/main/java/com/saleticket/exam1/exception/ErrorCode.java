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
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định!", HttpStatus.INTERNAL_SERVER_ERROR),

    EVENT_NOT_FOUND(1006, "Không tìm thấy sự kiện!", HttpStatus.NOT_FOUND),
    INVALID_EVENT_DATE(1007, "Ngày tổ chức sự kiện phải ở trong tương lai!", HttpStatus.BAD_REQUEST),
    SOLD_OUT(1008, "Rất tiếc! Số lượng vé còn lại không đủ đáp ứng yêu cầu của bạn.", HttpStatus.BAD_REQUEST),
    TICKET_LIMIT_EXCEEDED(1009, "Bạn chỉ được mua tối đa 5 vé cho mỗi sự kiện để tránh đầu cơ!",
            HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1010, "Không tìm thấy đơn hàng!", HttpStatus.NOT_FOUND),
    PAYMENT_STATUS_INVALID(1011,"Hóa đơn này không ở trạng thái chờ thanh toán!", HttpStatus.BAD_REQUEST)
    
    ;


    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
