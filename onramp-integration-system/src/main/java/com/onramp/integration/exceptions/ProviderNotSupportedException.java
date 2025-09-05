package com.onramp.integration.exceptions;

/**
 * Exception được ném khi nhà cung cấp dịch vụ không được hỗ trợ.
 */
public class ProviderNotSupportedException extends OnRampException {

    public ProviderNotSupportedException(String providerName) {
        super("PROVIDER_NOT_SUPPORTED", 
              "Nhà cung cấp dịch vụ không được hỗ trợ: " + providerName, 
              providerName);
    }

    public ProviderNotSupportedException(String providerName, Throwable cause) {
        super("PROVIDER_NOT_SUPPORTED", 
              "Nhà cung cấp dịch vụ không được hỗ trợ: " + providerName, 
              providerName, 
              cause);
    }
}

