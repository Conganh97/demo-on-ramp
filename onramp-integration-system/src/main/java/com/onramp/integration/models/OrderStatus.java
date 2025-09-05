package com.onramp.integration.models;

/**
 * Enum defining order statuses in the On-ramp system.
 */
public enum OrderStatus {
    /**
     * Order is waiting for payment from user.
     */
    PENDING_PAYMENT("pending_payment"),
    
    /**
     * Order is being processed.
     */
    PROCESSING("processing"),
    
    /**
     * Order completed successfully.
     */
    COMPLETED("completed"),
    
    /**
     * Order failed.
     */
    FAILED("failed"),
    
    /**
     * Order was cancelled.
     */
    CANCELLED("cancelled"),
    
    /**
     * Order has expired.
     */
    EXPIRED("expired");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts from string value to enum.
     * 
     * @param value String value
     * @return Corresponding OrderStatus
     * @throws IllegalArgumentException if no matching enum is found
     */
    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}

