package com.saleticket.exam1.exception;

import com.saleticket.exam1.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice//
public class GlobalExceptionHandler {

    // Bắt lỗi nghiệp vụ tự định nghĩa
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    // Bắt lỗi khi người dùng không có quyền (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // Bắt lỗi Validation (Khi @Valid DTO bị xịt)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder()
                        .code(400)
                        .message(errorMessage)
                        .build()
        );
    }

    // Lưới đánh cá cuối cùng: Bắt các lỗi chưa biết
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                        .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage() + " - " + ex.getMessage())
                        .build()
        );
    }
}