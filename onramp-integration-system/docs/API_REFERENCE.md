# API Reference - OnRamp Integration System

## Tổng quan

OnRamp Integration System cung cấp một bộ APIs thống nhất để tích hợp với nhiều nhà cung cấp dịch vụ fiat-to-crypto. Tài liệu này mô tả chi tiết các interfaces, methods, và data models của hệ thống.

## Core Interfaces

### OnRampService

Interface chính định nghĩa contract cho tất cả các nhà cung cấp dịch vụ on-ramp.

```java
public interface OnRampService {
    
    /**
     * Lấy danh sách các tài sản được hỗ trợ bởi provider.
     * 
     * @return CompletableFuture chứa danh sách Asset
     * @throws OnRampException nếu có lỗi khi gọi API
     */
    CompletableFuture<List<Asset>> getSupportedAssets();
    
    /**
     * Lấy báo giá cho việc mua tiền điện tử.
     * 
     * @param fiatCurrency Mã tiền pháp định (VD: "USD", "EUR")
     * @param cryptoCurrency Mã tiền điện tử (VD: "BTC", "ETH")
     * @param fiatAmount Số tiền pháp định (có thể null nếu có cryptoAmount)
     * @param cryptoAmount Số tiền điện tử (có thể null nếu có fiatAmount)
     * @return CompletableFuture chứa Quote
     * @throws IllegalArgumentException nếu parameters không hợp lệ
     * @throws OnRampException nếu có lỗi khi gọi API
     */
    CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                    Double fiatAmount, Double cryptoAmount);
    
    /**
     * Tạo đơn hàng mua tiền điện tử.
     * 
     * @param fiatCurrency Mã tiền pháp định
     * @param cryptoCurrency Mã tiền điện tử
     * @param fiatAmount Số tiền pháp định
     * @param cryptoAmount Số tiền điện tử
     * @param walletAddress Địa chỉ ví nhận tiền điện tử
     * @param redirectUrl URL để redirect sau khi hoàn thành thanh toán
     * @return CompletableFuture chứa Order
     * @throws IllegalArgumentException nếu parameters không hợp lệ
     * @throws OnRampException nếu có lỗi khi tạo đơn hàng
     */
    CompletableFuture<Order> createOrder(String fiatCurrency, String cryptoCurrency,
                                       Double fiatAmount, Double cryptoAmount,
                                       String walletAddress, String redirectUrl);
    
    /**
     * Lấy trạng thái của đơn hàng.
     * 
     * @param orderId ID của đơn hàng
     * @return CompletableFuture chứa Order với trạng thái cập nhật
     * @throws IllegalArgumentException nếu orderId null hoặc rỗng
     * @throws OnRampException nếu có lỗi khi lấy trạng thái
     */
    CompletableFuture<Order> getOrderStatus(String orderId);
    
    /**
     * Lấy danh sách phương thức thanh toán được hỗ trợ.
     * 
     * @param fiatCurrency Mã tiền pháp định
     * @param cryptoCurrency Mã tiền điện tử
     * @return CompletableFuture chứa danh sách PaymentMethod
     * @throws OnRampException nếu có lỗi khi gọi API
     */
    CompletableFuture<List<PaymentMethod>> getPaymentMethods(String fiatCurrency, String cryptoCurrency);
    
    /**
     * Lấy lịch sử giao dịch của user.
     * 
     * @param userId ID của user
     * @return CompletableFuture chứa danh sách Transaction
     * @throws OnRampException nếu có lỗi khi gọi API
     */
    CompletableFuture<List<Transaction>> getTransactionHistory(String userId);
    
    /**
     * Lấy tên của provider.
     * 
     * @return Tên provider (VD: "onramper", "moonpay")
     */
    String getProviderName();
    
    /**
     * Kiểm tra xem service có khả dụng không.
     * 
     * @return CompletableFuture<Boolean> true nếu service khả dụng
     */
    CompletableFuture<Boolean> isServiceAvailable();
    
    /**
     * Validate cấu hình của service.
     * 
     * @return CompletableFuture<Boolean> true nếu cấu hình hợp lệ
     */
    CompletableFuture<Boolean> validateConfiguration();
}
```

### OnRampServiceFactory

Abstract factory để tạo instances của OnRampService.

```java
public abstract class OnRampServiceFactory {
    
    /**
     * Tạo service instance cho provider cụ thể.
     * 
     * @param providerName Tên provider (VD: "onramper", "moonpay")
     * @param config Cấu hình cho provider
     * @return OnRampService instance
     * @throws ProviderNotSupportedException nếu provider không được hỗ trợ
     * @throws InvalidConfigurationException nếu cấu hình không hợp lệ
     */
    public abstract OnRampService createService(String providerName, OnRampConfig config);
    
    /**
     * Kiểm tra xem provider có được hỗ trợ không.
     * 
     * @param providerName Tên provider
     * @return true nếu provider được hỗ trợ
     */
    public abstract boolean isProviderSupported(String providerName);
    
    /**
     * Lấy danh sách tất cả providers được hỗ trợ.
     * 
     * @return Mảng tên providers
     */
    public abstract String[] getSupportedProviders();
    
    /**
     * Tạo service với cấu hình mặc định.
     * 
     * @param providerName Tên provider
     * @return OnRampService instance
     * @throws ProviderNotSupportedException nếu provider không được hỗ trợ
     */
    public abstract OnRampService createServiceWithDefaultConfig(String providerName);
}
```

### ConfigurableOnRampService

Interface để đánh dấu services có thể được cấu hình.

```java
public interface ConfigurableOnRampService {
    
    /**
     * Cấu hình service với OnRampConfig.
     * 
     * @param config Cấu hình cho service
     * @throws InvalidConfigurationException nếu cấu hình không hợp lệ
     */
    void configure(OnRampConfig config);
}
```

## Data Models

### OnRampConfig

Model chứa cấu hình cho một provider.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnRampConfig {
    
    /**
     * Tên nhà cung cấp dịch vụ.
     * Required: true
     * Example: "onramper", "moonpay"
     */
    @NotBlank(message = "Tên nhà cung cấp dịch vụ không được để trống")
    @JsonProperty("provider_name")
    private String providerName;
    
    /**
     * API key để xác thực với provider.
     * Required: true
     * Example: "pk_prod_01HETEQF46GSK6BS5JWKDF31BT"
     */
    @NotBlank(message = "API key không được để trống")
    @JsonProperty("api_key")
    private String apiKey;
    
    /**
     * API secret (nếu provider yêu cầu).
     * Required: false
     */
    @JsonProperty("api_secret")
    private String apiSecret;
    
    /**
     * URL cơ sở của API provider.
     * Required: true
     * Example: "https://api.onramper.com"
     */
    @NotBlank(message = "URL cơ sở của API không được để trống")
    @JsonProperty("base_url")
    private String baseUrl;
    
    /**
     * URL webhook để nhận notifications.
     * Required: false
     */
    @JsonProperty("webhook_url")
    private String webhookUrl;
    
    /**
     * Có sử dụng môi trường sandbox không.
     * Default: false
     */
    @JsonProperty("is_sandbox")
    @Builder.Default
    private Boolean isSandbox = false;
    
    /**
     * Timeout cho API calls (giây).
     * Default: 30
     * Range: 1-300
     */
    @NotNull(message = "Timeout không được null")
    @Positive(message = "Timeout phải lớn hơn 0")
    @JsonProperty("timeout")
    @Builder.Default
    private Integer timeout = 30;
    
    /**
     * Số lần thử lại khi API call thất bại.
     * Default: 3
     * Range: 0-10
     */
    @NotNull(message = "Số lần thử lại không được null")
    @JsonProperty("retry_attempts")
    @Builder.Default
    private Integer retryAttempts = 3;
    
    /**
     * Cấu hình bổ sung cho provider.
     * Required: false
     */
    @JsonProperty("additional_config")
    private Map<String, Object> additionalConfig;
    
    /**
     * Provider có được kích hoạt không.
     * Default: true
     */
    @JsonProperty("enabled")
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * Độ ưu tiên của provider (số nhỏ hơn = ưu tiên cao hơn).
     * Default: 1
     */
    @JsonProperty("priority")
    @Builder.Default
    private Integer priority = 1;
    
    /**
     * Mô tả về provider.
     * Required: false
     */
    @JsonProperty("description")
    private String description;
}
```

### Quote

Model chứa thông tin báo giá.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {
    
    /**
     * Mã tiền pháp định.
     * Example: "USD", "EUR"
     */
    private String fiatCurrency;
    
    /**
     * Mã tiền điện tử.
     * Example: "BTC", "ETH"
     */
    private String cryptoCurrency;
    
    /**
     * Số tiền pháp định.
     * Example: 100.0
     */
    private Double fiatAmount;
    
    /**
     * Số tiền điện tử tương ứng.
     * Example: 0.00234
     */
    private Double cryptoAmount;
    
    /**
     * Tỷ giá hối đoái.
     * Example: 42735.5 (1 BTC = 42735.5 USD)
     */
    private Double exchangeRate;
    
    /**
     * Phí giao dịch.
     * Example: 2.5
     */
    private Double fee;
    
    /**
     * Tổng số tiền pháp định cần thanh toán (bao gồm phí).
     * Example: 102.5
     */
    private Double totalFiatAmount;
    
    /**
     * Tên provider cung cấp báo giá.
     * Example: "onramper"
     */
    private String providerName;
    
    /**
     * Thời gian hết hạn của báo giá.
     */
    private LocalDateTime expiresAt;
    
    /**
     * Metadata bổ sung từ provider.
     */
    private Map<String, Object> metadata;
}
```

### Order

Model chứa thông tin đơn hàng.

```java
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    /**
     * ID duy nhất của đơn hàng trong hệ thống.
     */
    @Id
    private String orderId;
    
    /**
     * ID đơn hàng từ provider bên ngoài.
     */
    @Column(name = "external_order_id")
    private String externalOrderId;
    
    /**
     * Tên provider xử lý đơn hàng.
     */
    @Column(name = "provider_name")
    private String providerName;
    
    /**
     * Mã tiền pháp định.
     */
    @Column(name = "fiat_currency")
    private String fiatCurrency;
    
    /**
     * Mã tiền điện tử.
     */
    @Column(name = "crypto_currency")
    private String cryptoCurrency;
    
    /**
     * Số tiền pháp định.
     */
    @Column(name = "fiat_amount")
    private Double fiatAmount;
    
    /**
     * Số tiền điện tử.
     */
    @Column(name = "crypto_amount")
    private Double cryptoAmount;
    
    /**
     * Địa chỉ ví nhận tiền điện tử.
     */
    @Column(name = "wallet_address")
    private String walletAddress;
    
    /**
     * URL redirect sau khi hoàn thành.
     */
    @Column(name = "redirect_url")
    private String redirectUrl;
    
    /**
     * Trạng thái của đơn hàng.
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    /**
     * Thời gian tạo đơn hàng.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật cuối cùng.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Metadata bổ sung.
     */
    @ElementCollection
    @CollectionTable(name = "order_metadata", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, Object> metadata;
}
```

### OrderStatus

Enum định nghĩa các trạng thái của đơn hàng.

```java
public enum OrderStatus {
    /**
     * Đơn hàng đã được tạo, chờ thanh toán.
     */
    PENDING_PAYMENT("pending_payment", "Chờ thanh toán"),
    
    /**
     * Đang xử lý thanh toán.
     */
    PROCESSING("processing", "Đang xử lý"),
    
    /**
     * Thanh toán đã được xác nhận, chờ gửi crypto.
     */
    PAYMENT_CONFIRMED("payment_confirmed", "Đã xác nhận thanh toán"),
    
    /**
     * Đang gửi tiền điện tử.
     */
    SENDING_CRYPTO("sending_crypto", "Đang gửi tiền điện tử"),
    
    /**
     * Đơn hàng đã hoàn thành thành công.
     */
    COMPLETED("completed", "Hoàn thành"),
    
    /**
     * Đơn hàng thất bại.
     */
    FAILED("failed", "Thất bại"),
    
    /**
     * Đơn hàng đã bị hủy.
     */
    CANCELLED("cancelled", "Đã hủy"),
    
    /**
     * Đơn hàng hết hạn.
     */
    EXPIRED("expired", "Hết hạn");
    
    private final String code;
    private final String description;
    
    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Tìm OrderStatus từ code.
     * 
     * @param code Mã trạng thái
     * @return OrderStatus tương ứng
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}
```

### Asset

Model chứa thông tin về tài sản được hỗ trợ.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    
    /**
     * ID duy nhất của tài sản.
     */
    private String id;
    
    /**
     * Tên đầy đủ của tài sản.
     * Example: "Bitcoin", "Ethereum"
     */
    private String name;
    
    /**
     * Ký hiệu của tài sản.
     * Example: "BTC", "ETH"
     */
    private String symbol;
    
    /**
     * Loại tài sản.
     */
    private AssetType type;
    
    /**
     * Network/blockchain của tài sản.
     * Example: "bitcoin", "ethereum", "polygon"
     */
    private String network;
    
    /**
     * Số lượng tối thiểu có thể giao dịch.
     */
    private Double minAmount;
    
    /**
     * Số lượng tối đa có thể giao dịch.
     */
    private Double maxAmount;
    
    /**
     * Tài sản có khả dụng không.
     */
    private Boolean available;
    
    /**
     * Số thập phân được hỗ trợ.
     */
    private Integer decimals;
    
    /**
     * URL icon của tài sản.
     */
    private String iconUrl;
    
    /**
     * Metadata bổ sung.
     */
    private Map<String, Object> metadata;
}
```

### AssetType

Enum định nghĩa loại tài sản.

```java
public enum AssetType {
    /**
     * Tiền pháp định (USD, EUR, VND, etc.)
     */
    FIAT("fiat", "Tiền pháp định"),
    
    /**
     * Tiền điện tử (BTC, ETH, etc.)
     */
    CRYPTO("crypto", "Tiền điện tử");
    
    private final String code;
    private final String description;
    
    AssetType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### PaymentMethod

Model chứa thông tin phương thức thanh toán.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    
    /**
     * ID duy nhất của phương thức thanh toán.
     */
    private String id;
    
    /**
     * Tên phương thức thanh toán.
     * Example: "Credit Card", "Bank Transfer"
     */
    private String name;
    
    /**
     * Loại phương thức thanh toán.
     */
    private PaymentMethodType type;
    
    /**
     * URL icon của phương thức thanh toán.
     */
    private String iconUrl;
    
    /**
     * Số tiền tối thiểu.
     */
    private Double minLimit;
    
    /**
     * Số tiền tối đa.
     */
    private Double maxLimit;
    
    /**
     * Thời gian xử lý ước tính.
     * Example: "5-10 minutes", "1-3 business days"
     */
    private String processingTime;
    
    /**
     * Phương thức có khả dụng không.
     */
    private Boolean available;
    
    /**
     * Danh sách tiền tệ được hỗ trợ.
     */
    private List<String> supportedCurrencies;
    
    /**
     * Phí giao dịch (%).
     */
    private Double feePercentage;
    
    /**
     * Phí cố định.
     */
    private Double fixedFee;
    
    /**
     * Metadata bổ sung.
     */
    private Map<String, Object> metadata;
}
```

### PaymentMethodType

Enum định nghĩa loại phương thức thanh toán.

```java
public enum PaymentMethodType {
    CREDIT_CARD("credit_card", "Thẻ tín dụng"),
    DEBIT_CARD("debit_card", "Thẻ ghi nợ"),
    BANK_TRANSFER("bank_transfer", "Chuyển khoản ngân hàng"),
    APPLE_PAY("apple_pay", "Apple Pay"),
    GOOGLE_PAY("google_pay", "Google Pay"),
    SEPA("sepa", "SEPA"),
    IDEAL("ideal", "iDEAL"),
    SOFORT("sofort", "Sofort"),
    GIROPAY("giropay", "Giropay"),
    BANCONTACT("bancontact", "Bancontact"),
    EPS("eps", "EPS"),
    MULTIBANCO("multibanco", "Multibanco"),
    MYBANK("mybank", "MyBank"),
    PRZELEWY24("przelewy24", "Przelewy24"),
    TRUSTLY("trustly", "Trustly"),
    KLARNA("klarna", "Klarna"),
    OTHER("other", "Khác");
    
    private final String code;
    private final String description;
    
    PaymentMethodType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### Transaction

Model chứa thông tin giao dịch.

```java
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    /**
     * ID duy nhất của giao dịch.
     */
    @Id
    private String transactionId;
    
    /**
     * ID giao dịch từ provider.
     */
    @Column(name = "external_transaction_id")
    private String externalTransactionId;
    
    /**
     * ID đơn hàng liên quan.
     */
    @Column(name = "order_id")
    private String orderId;
    
    /**
     * ID user thực hiện giao dịch.
     */
    @Column(name = "user_id")
    private String userId;
    
    /**
     * Tên provider.
     */
    @Column(name = "provider_name")
    private String providerName;
    
    /**
     * Loại giao dịch.
     */
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    /**
     * Trạng thái giao dịch.
     */
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    /**
     * Mã tiền pháp định.
     */
    @Column(name = "fiat_currency")
    private String fiatCurrency;
    
    /**
     * Mã tiền điện tử.
     */
    @Column(name = "crypto_currency")
    private String cryptoCurrency;
    
    /**
     * Số tiền pháp định.
     */
    @Column(name = "fiat_amount")
    private Double fiatAmount;
    
    /**
     * Số tiền điện tử.
     */
    @Column(name = "crypto_amount")
    private Double cryptoAmount;
    
    /**
     * Phí giao dịch.
     */
    private Double fee;
    
    /**
     * Hash giao dịch trên blockchain.
     */
    @Column(name = "tx_hash")
    private String txHash;
    
    /**
     * Địa chỉ ví.
     */
    @Column(name = "wallet_address")
    private String walletAddress;
    
    /**
     * Thời gian tạo giao dịch.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật cuối.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Thời gian hoàn thành giao dịch.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Metadata bổ sung.
     */
    @ElementCollection
    @CollectionTable(name = "transaction_metadata", joinColumns = @JoinColumn(name = "transaction_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, Object> metadata;
}
```

### TransactionType

Enum định nghĩa loại giao dịch.

```java
public enum TransactionType {
    /**
     * Mua tiền điện tử bằng tiền pháp định.
     */
    BUY("buy", "Mua"),
    
    /**
     * Bán tiền điện tử lấy tiền pháp định.
     */
    SELL("sell", "Bán");
    
    private final String code;
    private final String description;
    
    TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### TransactionStatus

Enum định nghĩa trạng thái giao dịch.

```java
public enum TransactionStatus {
    /**
     * Giao dịch đang chờ xử lý.
     */
    PENDING("pending", "Chờ xử lý"),
    
    /**
     * Giao dịch đang được xử lý.
     */
    PROCESSING("processing", "Đang xử lý"),
    
    /**
     * Giao dịch đã hoàn thành.
     */
    COMPLETED("completed", "Hoàn thành"),
    
    /**
     * Giao dịch thất bại.
     */
    FAILED("failed", "Thất bại"),
    
    /**
     * Giao dịch đã bị hủy.
     */
    CANCELLED("cancelled", "Đã hủy");
    
    private final String code;
    private final String description;
    
    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
```

## DTOs (Data Transfer Objects)

### QuoteRequest

DTO cho request lấy báo giá.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
    
    /**
     * Mã tiền pháp định.
     * Required: true
     * Example: "USD"
     */
    @NotBlank(message = "Mã tiền pháp định không được để trống")
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    
    /**
     * Mã tiền điện tử.
     * Required: true
     * Example: "BTC"
     */
    @NotBlank(message = "Mã tiền điện tử không được để trống")
    @JsonProperty("crypto_currency")
    private String cryptoCurrency;
    
    /**
     * Số tiền pháp định.
     * Required: false (nếu có crypto_amount)
     * Example: 100.0
     */
    @JsonProperty("fiat_amount")
    private Double fiatAmount;
    
    /**
     * Số tiền điện tử.
     * Required: false (nếu có fiat_amount)
     * Example: 0.00234
     */
    @JsonProperty("crypto_amount")
    private Double cryptoAmount;
    
    /**
     * ID phương thức thanh toán.
     * Required: false
     * Example: "creditcard"
     */
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    /**
     * Mã quốc gia của user.
     * Required: false
     * Example: "US"
     */
    @JsonProperty("user_country")
    private String userCountry;
    
    /**
     * Tên provider (nếu muốn chỉ định cụ thể).
     * Required: false
     * Example: "onramper"
     */
    @JsonProperty("provider_name")
    private String providerName;
    
    /**
     * ID user.
     * Required: false
     */
    @JsonProperty("user_id")
    private String userId;
}
```

### OrderRequest

DTO cho request tạo đơn hàng.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    
    /**
     * Mã tiền pháp định.
     * Required: true
     */
    @NotBlank(message = "Mã tiền pháp định không được để trống")
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    
    /**
     * Mã tiền điện tử.
     * Required: true
     */
    @NotBlank(message = "Mã tiền điện tử không được để trống")
    @JsonProperty("crypto_currency")
    private String cryptoCurrency;
    
    /**
     * Số tiền pháp định.
     * Required: false
     */
    @JsonProperty("fiat_amount")
    private Double fiatAmount;
    
    /**
     * Số tiền điện tử.
     * Required: false
     */
    @JsonProperty("crypto_amount")
    private Double cryptoAmount;
    
    /**
     * Địa chỉ ví nhận tiền điện tử.
     * Required: true
     */
    @NotBlank(message = "Địa chỉ ví không được để trống")
    @JsonProperty("wallet_address")
    private String walletAddress;
    
    /**
     * ID phương thức thanh toán.
     * Required: false
     */
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    /**
     * URL chuyển hướng sau khi hoàn thành.
     * Required: true
     */
    @NotBlank(message = "URL chuyển hướng không được để trống")
    @JsonProperty("redirect_url")
    private String redirectUrl;
    
    /**
     * ID user.
     * Required: false
     */
    @JsonProperty("user_id")
    private String userId;
    
    /**
     * Email user.
     * Required: false
     */
    @JsonProperty("user_email")
    private String userEmail;
    
    /**
     * Mã quốc gia của user.
     * Required: false
     */
    @JsonProperty("user_country")
    private String userCountry;
    
    /**
     * Tên provider.
     * Required: false
     */
    @JsonProperty("provider_name")
    private String providerName;
}
```

## Exception Classes

### OnRampException

Base exception cho tất cả lỗi trong hệ thống.

```java
@Getter
public class OnRampException extends RuntimeException {
    
    /**
     * Mã lỗi.
     */
    private final String errorCode;
    
    /**
     * Tên provider gây ra lỗi.
     */
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
```

### ProviderNotSupportedException

Exception khi provider không được hỗ trợ.

```java
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
```

### InvalidConfigurationException

Exception khi cấu hình không hợp lệ.

```java
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
```

## Usage Examples

### Cơ bản

```java
// Tạo service factory
@Autowired
private OnRampServiceFactory serviceFactory;

// Tạo service với cấu hình mặc định
OnRampService service = serviceFactory.createServiceWithDefaultConfig("onramper");

// Lấy báo giá
CompletableFuture<Quote> quoteFuture = service.getQuote("USD", "BTC", 100.0, null);
Quote quote = quoteFuture.get();

System.out.println("Rate: " + quote.getExchangeRate());
System.out.println("Fee: " + quote.getFee());
System.out.println("Total: " + quote.getTotalFiatAmount());
```

### Tạo đơn hàng

```java
// Tạo đơn hàng
CompletableFuture<Order> orderFuture = service.createOrder(
    "USD", "BTC", 
    100.0, null,
    "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
    "https://yoursite.com/callback"
);

Order order = orderFuture.get();
System.out.println("Order ID: " + order.getOrderId());
System.out.println("Status: " + order.getStatus());
```

### Kiểm tra trạng thái đơn hàng

```java
// Kiểm tra trạng thái
CompletableFuture<Order> statusFuture = service.getOrderStatus(order.getOrderId());
Order updatedOrder = statusFuture.get();

System.out.println("Current status: " + updatedOrder.getStatus());
```

### Xử lý lỗi

```java
try {
    OnRampService service = serviceFactory.createService("invalid-provider", config);
} catch (ProviderNotSupportedException e) {
    System.err.println("Provider not supported: " + e.getProviderName());
} catch (InvalidConfigurationException e) {
    System.err.println("Invalid configuration: " + e.getMessage());
}

// Xử lý lỗi async
service.getQuote("USD", "BTC", 100.0, null)
    .handle((quote, throwable) -> {
        if (throwable != null) {
            if (throwable instanceof OnRampException) {
                OnRampException onRampException = (OnRampException) throwable;
                System.err.println("OnRamp error: " + onRampException.getErrorCode());
            }
            return null;
        }
        return quote;
    });
```

### Sử dụng với custom configuration

```java
OnRampConfig config = OnRampConfig.builder()
    .providerName("onramper")
    .apiKey("your-api-key")
    .baseUrl("https://api.onramper.com")
    .isSandbox(false)
    .timeout(60)
    .retryAttempts(5)
    .build();

OnRampService service = serviceFactory.createService("onramper", config);
```

## Error Codes

| Error Code | Description | Possible Causes |
|------------|-------------|-----------------|
| `GENERAL_ERROR` | Lỗi chung | Lỗi không xác định |
| `PROVIDER_NOT_SUPPORTED` | Provider không được hỗ trợ | Tên provider sai hoặc chưa được implement |
| `INVALID_CONFIGURATION` | Cấu hình không hợp lệ | API key sai, URL không hợp lệ |
| `API_ERROR` | Lỗi từ API provider | Provider API trả về lỗi |
| `NETWORK_ERROR` | Lỗi kết nối mạng | Timeout, connection refused |
| `VALIDATION_ERROR` | Lỗi validation input | Parameters không hợp lệ |
| `RATE_LIMIT_ERROR` | Vượt quá rate limit | Quá nhiều requests |
| `INSUFFICIENT_FUNDS` | Không đủ tiền | User không đủ tiền để thực hiện giao dịch |
| `CURRENCY_NOT_SUPPORTED` | Tiền tệ không được hỗ trợ | Cặp tiền tệ không có sẵn |
| `AMOUNT_TOO_LOW` | Số tiền quá thấp | Dưới minimum amount |
| `AMOUNT_TOO_HIGH` | Số tiền quá cao | Vượt quá maximum amount |

## Best Practices

### 1. Error Handling

```java
// Luôn handle exceptions trong async operations
service.getQuote("USD", "BTC", 100.0, null)
    .exceptionally(throwable -> {
        log.error("Error getting quote", throwable);
        return null; // hoặc default value
    });
```

### 2. Timeout Configuration

```java
// Set reasonable timeouts
OnRampConfig config = OnRampConfig.builder()
    .timeout(30) // 30 seconds
    .retryAttempts(3)
    .build();
```

### 3. Validation

```java
// Validate inputs trước khi gọi API
if (fiatAmount != null && fiatAmount <= 0) {
    throw new IllegalArgumentException("Fiat amount must be positive");
}
```

### 4. Logging

```java
// Log important events
log.info("Creating order: fiat={}, crypto={}, amount={}", 
    fiatCurrency, cryptoCurrency, fiatAmount);
```

### 5. Resource Management

```java
// Sử dụng try-with-resources khi cần
try (OnRampService service = serviceFactory.createService("onramper", config)) {
    // Use service
}
```

## Versioning

API này tuân theo [Semantic Versioning](https://semver.org/):

- **Major version**: Breaking changes
- **Minor version**: New features, backward compatible
- **Patch version**: Bug fixes, backward compatible

Current version: **1.0.0**

## Support

Để được hỗ trợ:

1. Kiểm tra [Documentation](README.md)
2. Xem [Developer Guide](DEVELOPER_GUIDE.md)
3. Tạo issue trên GitHub
4. Liên hệ support team

---

**Tác giả**: Manus AI  
**Phiên bản**: 1.0.0  
**Cập nhật lần cuối**: 2025-09-04

