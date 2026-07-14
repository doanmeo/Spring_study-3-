package com.saleticket.exam1.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public AppException(ErrorCode errorCode, String message) {
        super(errorCode.getMessage() + message);
        this.errorCode = errorCode;
    }

}

