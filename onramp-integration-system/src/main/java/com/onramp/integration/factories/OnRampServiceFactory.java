package com.onramp.integration.factories;

import com.onramp.integration.core.OnRampService;
import com.onramp.integration.models.OnRampConfig;

/**
 * Abstract Factory để tạo ra các instance của OnRampService.
 * Sử dụng Factory Pattern để tách biệt việc tạo object khỏi việc sử dụng object.
 */
public abstract class OnRampServiceFactory {

    /**
     * Tạo và trả về một instance của OnRampService dựa trên tên nhà cung cấp.
     * 
     * @param providerName Tên của nhà cung cấp dịch vụ (ví dụ: "onramper", "moonpay")
     * @param config Cấu hình cần thiết cho nhà cung cấp dịch vụ
     * @return Một instance của OnRampService
     * @throws IllegalArgumentException nếu providerName không được hỗ trợ
     * @throws IllegalStateException nếu không thể tạo service với cấu hình đã cho
     */
    public abstract OnRampService createService(String providerName, OnRampConfig config);

    /**
     * Kiểm tra xem nhà cung cấp có được hỗ trợ hay không.
     * 
     * @param providerName Tên nhà cung cấp dịch vụ
     * @return true nếu được hỗ trợ, false nếu không
     */
    public abstract boolean isProviderSupported(String providerName);

    /**
     * Lấy danh sách tất cả các nhà cung cấp được hỗ trợ.
     * 
     * @return Mảng chứa tên các nhà cung cấp được hỗ trợ
     */
    public abstract String[] getSupportedProviders();

    /**
     * Tạo service với cấu hình mặc định cho nhà cung cấp.
     * Sử dụng cấu hình từ application properties.
     * 
     * @param providerName Tên nhà cung cấp dịch vụ
     * @return Một instance của OnRampService với cấu hình mặc định
     * @throws IllegalArgumentException nếu providerName không được hỗ trợ
     * @throws IllegalStateException nếu không có cấu hình mặc định cho nhà cung cấp
     */
    public abstract OnRampService createServiceWithDefaultConfig(String providerName);
}

