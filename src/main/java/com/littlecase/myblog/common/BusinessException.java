package com.littlecase.myblog.common;

import lombok.Getter;

/**
 * 业务异常：在 Service / Controller 中抛出，由 GlobalExceptionHandler 统一捕获并转成 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
