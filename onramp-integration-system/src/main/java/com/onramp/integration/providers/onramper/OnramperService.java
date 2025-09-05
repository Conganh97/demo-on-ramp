package com.onramp.integration.providers.onramper;

import com.onramp.integration.core.OnRampService;
import com.onramp.integration.factories.DefaultOnRampServiceFactory.ConfigurableOnRampService;
import com.onramp.integration.models.*;
import com.onramp.integration.models.dto.QuoteRequest;
import com.onramp.integration.models.dto.OrderRequest;
import com.onramp.integration.exceptions.OnRampException;
import com.onramp.integration.exceptions.InvalidConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Concrete implementation của OnRampService cho nhà cung cấp Onramper.
 * Sử dụng Adapter Pattern để chuyển đổi API calls của Onramper thành interface chuẩn.
 */
@Service
@Slf4j
public class OnramperService implements OnRampService, ConfigurableOnRampService {

    private static final String PROVIDER_NAME = "onramper";
    private static final String PRODUCTION_BASE_URL = "https://api.onramper.com";
    private static final String STAGING_BASE_URL = "https://api-stg.onramper.com";

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private OnRampConfig config;

    @Autowired
    public OnramperService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public void configure(OnRampConfig config) {
        if (config == null) {
            throw new InvalidConfigurationException("Cấu hình không được null", PROVIDER_NAME);
        }

        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new InvalidConfigurationException("API key không được null hoặc rỗng", PROVIDER_NAME);
        }

        this.config = config;
        
        String baseUrl = config.getIsSandbox() ? STAGING_BASE_URL : PRODUCTION_BASE_URL;
        if (config.getBaseUrl() != null && !config.getBaseUrl().trim().isEmpty()) {
            baseUrl = config.getBaseUrl();
        }

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", config.getApiKey())
                .defaultHeader("Accept", "application/json")
                .build();

        log.info("Đã cấu hình OnramperService với base URL: {}", baseUrl);
    }

    @Override
    public CompletableFuture<List<Asset>> getSupportedAssets() {
        validateConfiguration();
        
        return webClient.get()
                .uri("/supported")
                .retrieve()
                .bodyToMono(OnramperSupportedResponse.class)
                .map(this::convertToAssets)
                .doOnSuccess(assets -> log.info("Đã lấy {} tài sản được hỗ trợ từ Onramper", assets.size()))
                .doOnError(error -> log.error("Lỗi khi lấy danh sách tài sản từ Onramper: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }

    @Override
    public CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                           Double fiatAmount, Double cryptoAmount) {
        validateConfiguration();
        validateQuoteParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount);

        String uri = String.format("/quotes/%s/%s", fiatCurrency.toLowerCase(), cryptoCurrency.toLowerCase());
        
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(uri);
                    if (fiatAmount != null) {
                        builder.queryParam("amount", fiatAmount);
                    }
                    // Có thể thêm các parameters khác như paymentMethod, country, etc.
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(OnramperQuoteResponse.class)
                .map(response -> convertToQuote(response, fiatCurrency, cryptoCurrency))
                .doOnSuccess(quote -> log.info("Đã lấy báo giá từ Onramper: {} {} -> {} {}", 
                    quote.getFiatAmount(), quote.getFiatCurrency(), 
                    quote.getCryptoAmount(), quote.getCryptoCurrency()))
                .doOnError(error -> log.error("Lỗi khi lấy báo giá từ Onramper: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }

    @Override
    public CompletableFuture<Order> createOrder(String fiatCurrency, String cryptoCurrency,
                                              Double fiatAmount, Double cryptoAmount,
                                              String walletAddress, String redirectUrl) {
        validateConfiguration();
        validateOrderParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount, walletAddress, redirectUrl);

        OnramperOrderRequest request = OnramperOrderRequest.builder()
                .fiatCurrency(fiatCurrency)
                .cryptoCurrency(cryptoCurrency)
                .fiatAmount(fiatAmount)
                .cryptoAmount(cryptoAmount)
                .walletAddress(walletAddress)
                .redirectUrl(redirectUrl)
                .build();

        return webClient.post()
                .uri("/checkout/intent")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OnramperOrderResponse.class)
                .map(this::convertToOrder)
                .doOnSuccess(order -> log.info("Đã tạo đơn hàng Onramper: {}", order.getOrderId()))
                .doOnError(error -> log.error("Lỗi khi tạo đơn hàng Onramper: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }

    @Override
    public CompletableFuture<Order> getOrderStatus(String orderId) {
        validateConfiguration();
        
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID không được null hoặc rỗng");
        }

        return webClient.get()
                .uri("/transactions/{transactionId}", orderId)
                .header("x-onramper-secret", config.getApiSecret()) // Nếu cần
                .retrieve()
                .bodyToMono(OnramperTransactionResponse.class)
                .map(this::convertTransactionToOrder)
                .doOnSuccess(order -> log.info("Đã lấy trạng thái đơn hàng Onramper: {} - {}", 
                    order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("Lỗi khi lấy trạng thái đơn hàng Onramper: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }

    @Override
    public CompletableFuture<List<PaymentMethod>> getPaymentMethods(String fiatCurrency, String cryptoCurrency) {
        validateConfiguration();
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/payments")
                    .queryParam("fiat", fiatCurrency)
                    .queryParam("crypto", cryptoCurrency)
                    .build())
                .retrieve()
                .bodyToMono(OnramperPaymentMethodsResponse.class)
                .map(this::convertToPaymentMethods)
                .doOnSuccess(methods -> log.info("Đã lấy {} phương thức thanh toán từ Onramper", methods.size()))
                .doOnError(error -> log.error("Lỗi khi lấy phương thức thanh toán từ Onramper: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }

    @Override
    public CompletableFuture<List<Transaction>> getTransactionHistory(String userId) {
        validateConfiguration();
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/transactions")
                    .queryParam("userId", userId)
                    .build())
                .retrieve()
                .bodyToMono(OnramperTransactionHistoryResponse.class)
                .map(this::convertToTransactions)
                .doOnSuccess(transactions -> log.info("Đã lấy {} giao dịch từ Onramper cho user {}", 
                    transactions.size(), userId))
                .doOnError(error -> log.error("Lỗi khi lấy lịch sử giao dịch từ Onramper: {}", error.getMessage()))
                .onErrorMap(this::handleWebClientError)
                .toFuture();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public CompletableFuture<Boolean> isServiceAvailable() {
        if (config == null || webClient == null) {
            return CompletableFuture.completedFuture(false);
        }

        return webClient.get()
                .uri("/supported")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorReturn(false)
                .toFuture();
    }

    @Override
    public CompletableFuture<Boolean> validateConfiguration() {
        if (config == null) {
            return CompletableFuture.completedFuture(false);
        }

        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(true);
    }

    // Private helper methods

    private void validateConfiguration() {
        if (config == null || webClient == null) {
            throw new InvalidConfigurationException("Service chưa được cấu hình", PROVIDER_NAME);
        }
    }

    private void validateQuoteParameters(String fiatCurrency, String cryptoCurrency, 
                                       Double fiatAmount, Double cryptoAmount) {
        if (fiatCurrency == null || fiatCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tiền pháp định không được null hoặc rỗng");
        }
        if (cryptoCurrency == null || cryptoCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tiền điện tử không được null hoặc rỗng");
        }
        if (fiatAmount == null && cryptoAmount == null) {
            throw new IllegalArgumentException("Phải cung cấp fiatAmount hoặc cryptoAmount");
        }
        if (fiatAmount != null && cryptoAmount != null) {
            throw new IllegalArgumentException("Chỉ có thể cung cấp fiatAmount hoặc cryptoAmount, không phải cả hai");
        }
    }

    private void validateOrderParameters(String fiatCurrency, String cryptoCurrency,
                                       Double fiatAmount, Double cryptoAmount,
                                       String walletAddress, String redirectUrl) {
        validateQuoteParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount);
        
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ ví không được null hoặc rỗng");
        }
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL chuyển hướng không được null hoặc rỗng");
        }
    }

    private Throwable handleWebClientError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webError = (WebClientResponseException) error;
            String errorMessage = String.format("Lỗi API Onramper: %d - %s", 
                webError.getStatusCode().value(), webError.getResponseBodyAsString());
            return new OnRampException("API_ERROR", errorMessage, PROVIDER_NAME, error);
        }
        return new OnRampException("NETWORK_ERROR", "Lỗi kết nối đến Onramper: " + error.getMessage(), PROVIDER_NAME, error);
    }

    // Conversion methods - sẽ được implement dựa trên response format thực tế của Onramper
    private List<Asset> convertToAssets(OnramperSupportedResponse response) {
        // TODO: Implement conversion logic based on actual Onramper response format
        return List.of();
    }

    private Quote convertToQuote(OnramperQuoteResponse response, String fiatCurrency, String cryptoCurrency) {
        // TODO: Implement conversion logic based on actual Onramper response format
        return Quote.builder()
                .fiatCurrency(fiatCurrency)
                .cryptoCurrency(cryptoCurrency)
                .providerName(PROVIDER_NAME)
                .build();
    }

    private Order convertToOrder(OnramperOrderResponse response) {
        // TODO: Implement conversion logic based on actual Onramper response format
        return Order.builder()
                .providerName(PROVIDER_NAME)
                .status(OrderStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Order convertTransactionToOrder(OnramperTransactionResponse response) {
        // TODO: Implement conversion logic based on actual Onramper response format
        return Order.builder()
                .providerName(PROVIDER_NAME)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private List<PaymentMethod> convertToPaymentMethods(OnramperPaymentMethodsResponse response) {
        // TODO: Implement conversion logic based on actual Onramper response format
        return List.of();
    }

    private List<Transaction> convertToTransactions(OnramperTransactionHistoryResponse response) {
        // TODO: Implement conversion logic based on actual Onramper response format
        return List.of();
    }
}

