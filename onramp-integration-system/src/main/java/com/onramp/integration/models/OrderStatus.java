package com.onramp.integration.models;

/**
 * Enum định nghĩa các trạng thái đơn hàng trong hệ thống On-ramp.
 */
public enum OrderStatus {
    /**
     * Đơn hàng đang chờ thanh toán từ người dùng.
     */
    PENDING_PAYMENT("pending_payment"),
    
    /**
     * Đơn hàng đang được xử lý.
     */
    PROCESSING("processing"),
    
    /**
     * Đơn hàng đã hoàn thành thành công.
     */
    COMPLETED("completed"),
    
    /**
     * Đơn hàng thất bại.
     */
    FAILED("failed"),
    
    /**
     * Đơn hàng đã bị hủy.
     */
    CANCELLED("cancelled"),
    
    /**
     * Đơn hàng đã hết hạn.
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
     * Chuyển đổi từ string value sang enum.
     * 
     * @param value Giá trị string
     * @return OrderStatus tương ứng
     * @throws IllegalArgumentException nếu không tìm thấy enum phù hợp
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

