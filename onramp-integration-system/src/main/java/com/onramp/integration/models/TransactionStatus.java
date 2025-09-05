package com.onramp.integration.models;

/**
 * Enum defining transaction statuses in the On-ramp system.
 */
public enum TransactionStatus {
    /**
     * Transaction is pending processing.
     */
    PENDING("pending"),
    
    /**
     * Transaction successful.
     */
    SUCCESS("success"),
    
    /**
     * Transaction failed.
     */
    FAILED("failed"),
    
    /**
     * Transaction was cancelled.
     */
    CANCELLED("cancelled");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts from string value to enum.
     * 
     * @param value String value
     * @return Corresponding TransactionStatus
     * @throws IllegalArgumentException if no matching enum is found
     */
    public static TransactionStatus fromValue(String value) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TransactionStatus value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}

