package com.onramp.integration.models;

/**
 * Enum định nghĩa các trạng thái giao dịch trong hệ thống On-ramp.
 */
public enum TransactionStatus {
    /**
     * Giao dịch đang chờ xử lý.
     */
    PENDING("pending"),
    
    /**
     * Giao dịch thành công.
     */
    SUCCESS("success"),
    
    /**
     * Giao dịch thất bại.
     */
    FAILED("failed"),
    
    /**
     * Giao dịch đã bị hủy.
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
     * Chuyển đổi từ string value sang enum.
     * 
     * @param value Giá trị string
     * @return TransactionStatus tương ứng
     * @throws IllegalArgumentException nếu không tìm thấy enum phù hợp
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

