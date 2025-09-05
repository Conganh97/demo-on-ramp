package com.onramp.integration.exceptions;

import lombok.Getter;

/**
 * Base exception cho tất cả các lỗi trong hệ thống On-ramp.
 */
@Getter
public class OnRampException extends RuntimeException {
    
    private final String errorCode;
    private final String providerName;

    public OnRampException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
        this.providerName = null;
    }

    public OnRampException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
        this.providerName = null;
    }

    public OnRampException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.providerName = null;
    }

    public OnRampException(String errorCode, String message, String providerName) {
        super(message);
        this.errorCode = errorCode;
        this.providerName = providerName;
    }

    public OnRampException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.providerName = null;
    }

    public OnRampException(String errorCode, String message, String providerName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.providerName = providerName;
    }
}

