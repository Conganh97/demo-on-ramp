package com.onramp.integration.exceptions;

/**
 * Exception thrown when service provider is not supported.
 */
public class ProviderNotSupportedException extends OnRampException {

    public ProviderNotSupportedException(String providerName) {
        super("PROVIDER_NOT_SUPPORTED", 
              "Service provider not supported: " + providerName, 
              providerName);
    }

    public ProviderNotSupportedException(String providerName, Throwable cause) {
        super("PROVIDER_NOT_SUPPORTED", 
              "Service provider not supported: " + providerName, 
              providerName, 
              cause);
    }
}

