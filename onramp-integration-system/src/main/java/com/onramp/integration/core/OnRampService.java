package com.onramp.integration.core;

import com.onramp.integration.models.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Giao diện chung cho tất cả các dịch vụ On-ramp.
 * Định nghĩa hợp đồng mà mọi nhà cung cấp dịch vụ On-ramp phải tuân theo.
 * 
 * Sử dụng Strategy Pattern để cho phép hoán đổi giữa các nhà cung cấp khác nhau.
 */
public interface OnRampService {

    /**
     * Lấy danh sách các tài sản (tiền điện tử và tiền pháp định) được hỗ trợ.
     * 
     * @return CompletableFuture chứa danh sách các tài sản được hỗ trợ
     */
    CompletableFuture<List<Asset>> getSupportedAssets();

    /**
     * Lấy báo giá cho một giao dịch mua tiền điện tử.
     * Cần cung cấp fiatAmount HOẶC cryptoAmount.
     * 
     * @param fiatCurrency Mã tiền pháp định (ví dụ: USD, VND)
     * @param cryptoCurrency Mã tiền điện tử (ví dụ: BTC, ETH)
     * @param fiatAmount Số tiền pháp định (optional, nếu biết trước)
     * @param cryptoAmount Số lượng tiền điện tử (optional, nếu biết trước)
     * @return CompletableFuture chứa thông tin báo giá
     * @throws IllegalArgumentException nếu cả fiatAmount và cryptoAmount đều null hoặc cả hai đều có giá trị
     */
    CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                     Double fiatAmount, Double cryptoAmount);

    /**
     * Tạo một đơn hàng mua tiền điện tử.
     * Cần cung cấp fiatAmount HOẶC cryptoAmount.
     * 
     * @param fiatCurrency Mã tiền pháp định
     * @param cryptoCurrency Mã tiền điện tử
     * @param fiatAmount Số tiền pháp định (optional)
     * @param cryptoAmount Số lượng tiền điện tử (optional)
     * @param walletAddress Địa chỉ ví nhận tiền điện tử
     * @param redirectUrl URL để chuyển hướng sau khi hoàn tất thanh toán
     * @return CompletableFuture chứa thông tin đơn hàng đã tạo
     * @throws IllegalArgumentException nếu cả fiatAmount và cryptoAmount đều null hoặc cả hai đều có giá trị
     */
    CompletableFuture<Order> createOrder(String fiatCurrency, String cryptoCurrency,
                                        Double fiatAmount, Double cryptoAmount,
                                        String walletAddress, String redirectUrl);

    /**
     * Lấy trạng thái của một đơn hàng đã tạo.
     * 
     * @param orderId ID của đơn hàng
     * @return CompletableFuture chứa thông tin trạng thái đơn hàng
     * @throws IllegalArgumentException nếu orderId null hoặc rỗng
     */
    CompletableFuture<Order> getOrderStatus(String orderId);

    /**
     * Lấy danh sách các phương thức thanh toán được hỗ trợ cho cặp tiền tệ cụ thể.
     * 
     * @param fiatCurrency Mã tiền pháp định
     * @param cryptoCurrency Mã tiền điện tử
     * @return CompletableFuture chứa danh sách các phương thức thanh toán
     */
    CompletableFuture<List<PaymentMethod>> getPaymentMethods(String fiatCurrency, String cryptoCurrency);

    /**
     * Lấy lịch sử giao dịch của người dùng.
     * Lưu ý: Không phải tất cả các nhà cung cấp đều hỗ trợ chức năng này qua API.
     * 
     * @param userId ID của người dùng
     * @return CompletableFuture chứa danh sách các giao dịch
     * @throws UnsupportedOperationException nếu nhà cung cấp không hỗ trợ chức năng này
     */
    CompletableFuture<List<Transaction>> getTransactionHistory(String userId);

    /**
     * Lấy tên của nhà cung cấp dịch vụ.
     * 
     * @return Tên nhà cung cấp dịch vụ
     */
    String getProviderName();

    /**
     * Kiểm tra xem dịch vụ có khả dụng hay không.
     * 
     * @return CompletableFuture chứa true nếu dịch vụ khả dụng, false nếu không
     */
    CompletableFuture<Boolean> isServiceAvailable();

    /**
     * Xác thực cấu hình của dịch vụ.
     * 
     * @return CompletableFuture chứa true nếu cấu hình hợp lệ, false nếu không
     */
    CompletableFuture<Boolean> validateConfiguration();
}

