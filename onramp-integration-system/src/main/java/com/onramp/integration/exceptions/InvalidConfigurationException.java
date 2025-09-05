package com.onramp.integration.exceptions;

/**
 * Exception được ném khi cấu hình của nhà cung cấp dịch vụ không hợp lệ.
 */
public class InvalidConfigurationException extends OnRampException {

    public InvalidConfigurationException(String message) {
        super("INVALID_CONFIGURATION", message);
    }

    public InvalidConfigurationException(String message, String providerName) {
        super("INVALID_CONFIGURATION", message, providerName);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super("INVALID_CONFIGURATION", message, cause);
    }

    public InvalidConfigurationException(String message, String providerName, Throwable cause) {
        super("INVALID_CONFIGURATION", message, providerName, cause);
    }
}

