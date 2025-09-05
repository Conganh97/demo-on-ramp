package com.onramp.integration.demo;

import com.onramp.integration.core.OnRampService;
import com.onramp.integration.factories.OnRampServiceFactory;
import com.onramp.integration.models.*;
import com.onramp.integration.exceptions.OnRampException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Ứng dụng demo để minh họa cách sử dụng OnRamp Integration System.
 * 
 * Ứng dụng này cho phép user:
 * 1. Xem danh sách providers được hỗ trợ
 * 2. Lấy báo giá từ provider
 * 3. Tạo đơn hàng mua tiền điện tử
 * 4. Kiểm tra trạng thái đơn hàng
 * 5. Xem lịch sử giao dịch
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.onramp.integration")
public class OnRampDemo implements CommandLineRunner {

    @Autowired
    private OnRampServiceFactory serviceFactory;

    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SpringApplication.run(OnRampDemo.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("🚀 OnRamp Integration System Demo");
        System.out.println("=".repeat(60));
        System.out.println();

        try {
            showMainMenu();
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n📋 MENU CHÍNH");
            System.out.println("1. Xem danh sách providers");
            System.out.println("2. Lấy báo giá");
            System.out.println("3. Tạo đơn hàng");
            System.out.println("4. Kiểm tra trạng thái đơn hàng");
            System.out.println("5. Xem tài sản được hỗ trợ");
            System.out.println("6. Xem phương thức thanh toán");
            System.out.println("7. Test kết nối provider");
            System.out.println("0. Thoát");
            System.out.print("\nChọn tùy chọn (0-7): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        showSupportedProviders();
                        break;
                    case 2:
                        getQuoteDemo();
                        break;
                    case 3:
                        createOrderDemo();
                        break;
                    case 4:
                        checkOrderStatusDemo();
                        break;
                    case 5:
                        showSupportedAssetsDemo();
                        break;
                    case 6:
                        showPaymentMethodsDemo();
                        break;
                    case 7:
                        testProviderConnectionDemo();
                        break;
                    case 0:
                        System.out.println("\n👋 Cảm ơn bạn đã sử dụng OnRamp Integration System Demo!");
                        return;
                    default:
                        System.out.println("❌ Tùy chọn không hợp lệ. Vui lòng chọn từ 0-7.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số hợp lệ.");
            } catch (Exception e) {
                System.err.println("❌ Lỗi: " + e.getMessage());
            }
        }
    }

    private void showSupportedProviders() {
        System.out.println("\n🏢 DANH SÁCH PROVIDERS ĐƯỢC HỖ TRỢ");
        System.out.println("-".repeat(40));

        String[] providers = serviceFactory.getSupportedProviders();
        
        if (providers.length == 0) {
            System.out.println("❌ Không có provider nào được hỗ trợ.");
            return;
        }

        for (int i = 0; i < providers.length; i++) {
            String provider = providers[i];
            boolean isSupported = serviceFactory.isProviderSupported(provider);
            String status = isSupported ? "✅ Được hỗ trợ" : "❌ Không được hỗ trợ";
            
            System.out.printf("%d. %s - %s%n", i + 1, provider.toUpperCase(), status);
        }
    }

    private void getQuoteDemo() {
        System.out.println("\n💰 LẤY BÁO GIÁ");
        System.out.println("-".repeat(30));

        try {
            // Chọn provider
            String provider = selectProvider();
            if (provider == null) return;

            // Nhập thông tin báo giá
            System.out.print("Nhập mã tiền pháp định (VD: USD): ");
            String fiatCurrency = scanner.nextLine().trim().toUpperCase();

            System.out.print("Nhập mã tiền điện tử (VD: BTC): ");
            String cryptoCurrency = scanner.nextLine().trim().toUpperCase();

            System.out.print("Nhập số tiền pháp định (VD: 100): ");
            String amountStr = scanner.nextLine().trim();
            Double fiatAmount = Double.parseDouble(amountStr);

            // Tạo service và lấy báo giá
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            System.out.println("\n⏳ Đang lấy báo giá...");
            
            CompletableFuture<Quote> quoteFuture = service.getQuote(fiatCurrency, cryptoCurrency, fiatAmount, null);
            Quote quote = quoteFuture.get();

            // Hiển thị kết quả
            System.out.println("\n✅ BÁO GIÁ THÀNH CÔNG");
            System.out.println("-".repeat(30));
            System.out.printf("Provider: %s%n", quote.getProviderName());
            System.out.printf("Tiền pháp định: %s %.2f%n", quote.getFiatCurrency(), quote.getFiatAmount());
            System.out.printf("Tiền điện tử: %s %.8f%n", quote.getCryptoCurrency(), quote.getCryptoAmount());
            System.out.printf("Tỷ giá: %.2f %s/%s%n", quote.getExchangeRate(), quote.getFiatCurrency(), quote.getCryptoCurrency());
            System.out.printf("Phí: %.2f %s%n", quote.getFee(), quote.getFiatCurrency());
            System.out.printf("Tổng cộng: %.2f %s%n", quote.getTotalFiatAmount(), quote.getFiatCurrency());
            
            if (quote.getExpiresAt() != null) {
                System.out.printf("Hết hạn: %s%n", quote.getExpiresAt());
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy báo giá: " + e.getMessage());
        }
    }

    private void createOrderDemo() {
        System.out.println("\n🛒 TẠO ĐƠN HÀNG");
        System.out.println("-".repeat(30));

        try {
            // Chọn provider
            String provider = selectProvider();
            if (provider == null) return;

            // Nhập thông tin đơn hàng
            System.out.print("Nhập mã tiền pháp định (VD: USD): ");
            String fiatCurrency = scanner.nextLine().trim().toUpperCase();

            System.out.print("Nhập mã tiền điện tử (VD: BTC): ");
            String cryptoCurrency = scanner.nextLine().trim().toUpperCase();

            System.out.print("Nhập số tiền pháp định (VD: 100): ");
            String amountStr = scanner.nextLine().trim();
            Double fiatAmount = Double.parseDouble(amountStr);

            System.out.print("Nhập địa chỉ ví (VD: 1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa): ");
            String walletAddress = scanner.nextLine().trim();

            System.out.print("Nhập URL callback (VD: https://yoursite.com/callback): ");
            String redirectUrl = scanner.nextLine().trim();

            // Tạo service và đơn hàng
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            System.out.println("\n⏳ Đang tạo đơn hàng...");
            
            CompletableFuture<Order> orderFuture = service.createOrder(
                fiatCurrency, cryptoCurrency, 
                fiatAmount, null,
                walletAddress, redirectUrl
            );
            Order order = orderFuture.get();

            // Hiển thị kết quả
            System.out.println("\n✅ TẠO ĐƠN HÀNG THÀNH CÔNG");
            System.out.println("-".repeat(40));
            System.out.printf("Order ID: %s%n", order.getOrderId());
            System.out.printf("External Order ID: %s%n", order.getExternalOrderId());
            System.out.printf("Provider: %s%n", order.getProviderName());
            System.out.printf("Trạng thái: %s%n", order.getStatus());
            System.out.printf("Tiền pháp định: %s %.2f%n", order.getFiatCurrency(), order.getFiatAmount());
            System.out.printf("Tiền điện tử: %s %.8f%n", order.getCryptoCurrency(), order.getCryptoAmount());
            System.out.printf("Địa chỉ ví: %s%n", order.getWalletAddress());
            System.out.printf("Thời gian tạo: %s%n", order.getCreatedAt());

            System.out.println("\n💡 Lưu Order ID để kiểm tra trạng thái sau: " + order.getOrderId());

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo đơn hàng: " + e.getMessage());
        }
    }

    private void checkOrderStatusDemo() {
        System.out.println("\n🔍 KIỂM TRA TRẠNG THÁI ĐƠN HÀNG");
        System.out.println("-".repeat(40));

        try {
            // Chọn provider
            String provider = selectProvider();
            if (provider == null) return;

            System.out.print("Nhập Order ID: ");
            String orderId = scanner.nextLine().trim();

            if (orderId.isEmpty()) {
                System.out.println("❌ Order ID không được để trống.");
                return;
            }

            // Tạo service và kiểm tra trạng thái
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            System.out.println("\n⏳ Đang kiểm tra trạng thái...");
            
            CompletableFuture<Order> orderFuture = service.getOrderStatus(orderId);
            Order order = orderFuture.get();

            // Hiển thị kết quả
            System.out.println("\n✅ TRẠNG THÁI ĐƠN HÀNG");
            System.out.println("-".repeat(30));
            System.out.printf("Order ID: %s%n", order.getOrderId());
            System.out.printf("Trạng thái: %s%n", order.getStatus());
            System.out.printf("Provider: %s%n", order.getProviderName());
            System.out.printf("Cập nhật lần cuối: %s%n", order.getUpdatedAt());

            // Hiển thị thông tin bổ sung dựa trên trạng thái
            switch (order.getStatus()) {
                case PENDING_PAYMENT:
                    System.out.println("💡 Đơn hàng đang chờ thanh toán.");
                    break;
                case PROCESSING:
                    System.out.println("⚙️ Đơn hàng đang được xử lý.");
                    break;
                case COMPLETED:
                    System.out.println("🎉 Đơn hàng đã hoàn thành thành công!");
                    break;
                case FAILED:
                    System.out.println("❌ Đơn hàng đã thất bại.");
                    break;
                case CANCELLED:
                    System.out.println("🚫 Đơn hàng đã bị hủy.");
                    break;
                default:
                    System.out.println("ℹ️ Trạng thái: " + order.getStatus());
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi kiểm tra trạng thái: " + e.getMessage());
        }
    }

    private void showSupportedAssetsDemo() {
        System.out.println("\n💎 TÀI SẢN ĐƯỢC HỖ TRỢ");
        System.out.println("-".repeat(30));

        try {
            // Chọn provider
            String provider = selectProvider();
            if (provider == null) return;

            // Tạo service và lấy danh sách tài sản
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            System.out.println("\n⏳ Đang lấy danh sách tài sản...");
            
            CompletableFuture<List<Asset>> assetsFuture = service.getSupportedAssets();
            List<Asset> assets = assetsFuture.get();

            // Hiển thị kết quả
            System.out.println("\n✅ DANH SÁCH TÀI SẢN");
            System.out.println("-".repeat(50));
            System.out.printf("Tổng số tài sản: %d%n%n", assets.size());

            if (assets.isEmpty()) {
                System.out.println("❌ Không có tài sản nào được hỗ trợ.");
                return;
            }

            // Hiển thị 10 tài sản đầu tiên
            int displayCount = Math.min(10, assets.size());
            for (int i = 0; i < displayCount; i++) {
                Asset asset = assets.get(i);
                System.out.printf("%d. %s (%s) - %s%n", 
                    i + 1, 
                    asset.getName() != null ? asset.getName() : "N/A",
                    asset.getSymbol() != null ? asset.getSymbol() : "N/A",
                    asset.getType() != null ? asset.getType() : "N/A"
                );
            }

            if (assets.size() > 10) {
                System.out.printf("... và %d tài sản khác%n", assets.size() - 10);
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách tài sản: " + e.getMessage());
        }
    }

    private void showPaymentMethodsDemo() {
        System.out.println("\n💳 PHƯƠNG THỨC THANH TOÁN");
        System.out.println("-".repeat(35));

        try {
            // Chọn provider
            String provider = selectProvider();
            if (provider == null) return;

            System.out.print("Nhập mã tiền pháp định (VD: USD): ");
            String fiatCurrency = scanner.nextLine().trim().toUpperCase();

            System.out.print("Nhập mã tiền điện tử (VD: BTC): ");
            String cryptoCurrency = scanner.nextLine().trim().toUpperCase();

            // Tạo service và lấy phương thức thanh toán
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            System.out.println("\n⏳ Đang lấy phương thức thanh toán...");
            
            CompletableFuture<List<PaymentMethod>> methodsFuture = service.getPaymentMethods(fiatCurrency, cryptoCurrency);
            List<PaymentMethod> methods = methodsFuture.get();

            // Hiển thị kết quả
            System.out.println("\n✅ PHƯƠNG THỨC THANH TOÁN");
            System.out.println("-".repeat(40));
            System.out.printf("Cặp tiền tệ: %s/%s%n", fiatCurrency, cryptoCurrency);
            System.out.printf("Số phương thức: %d%n%n", methods.size());

            if (methods.isEmpty()) {
                System.out.println("❌ Không có phương thức thanh toán nào cho cặp tiền tệ này.");
                return;
            }

            for (int i = 0; i < methods.size(); i++) {
                PaymentMethod method = methods.get(i);
                System.out.printf("%d. %s%n", i + 1, method.getName() != null ? method.getName() : "N/A");
                
                if (method.getMinLimit() != null && method.getMaxLimit() != null) {
                    System.out.printf("   Giới hạn: %.2f - %.2f %s%n", 
                        method.getMinLimit(), method.getMaxLimit(), fiatCurrency);
                }
                
                if (method.getProcessingTime() != null) {
                    System.out.printf("   Thời gian xử lý: %s%n", method.getProcessingTime());
                }
                
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy phương thức thanh toán: " + e.getMessage());
        }
    }

    private void testProviderConnectionDemo() {
        System.out.println("\n🔗 TEST KẾT NỐI PROVIDER");
        System.out.println("-".repeat(35));

        try {
            // Chọn provider
            String provider = selectProvider();
            if (provider == null) return;

            // Tạo service và test kết nối
            OnRampService service = serviceFactory.createServiceWithDefaultConfig(provider);
            
            System.out.println("\n⏳ Đang test kết nối...");
            
            // Test validate configuration
            CompletableFuture<Boolean> configValidFuture = service.validateConfiguration();
            boolean configValid = configValidFuture.get();

            // Test service availability
            CompletableFuture<Boolean> availableFuture = service.isServiceAvailable();
            boolean available = availableFuture.get();

            // Hiển thị kết quả
            System.out.println("\n📊 KẾT QUẢ TEST");
            System.out.println("-".repeat(25));
            System.out.printf("Provider: %s%n", provider.toUpperCase());
            System.out.printf("Cấu hình hợp lệ: %s%n", configValid ? "✅ Có" : "❌ Không");
            System.out.printf("Service khả dụng: %s%n", available ? "✅ Có" : "❌ Không");

            if (configValid && available) {
                System.out.println("\n🎉 Provider hoạt động bình thường!");
            } else {
                System.out.println("\n⚠️ Provider có vấn đề. Kiểm tra cấu hình và kết nối mạng.");
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi test kết nối: " + e.getMessage());
        }
    }

    private String selectProvider() {
        String[] providers = serviceFactory.getSupportedProviders();
        
        if (providers.length == 0) {
            System.out.println("❌ Không có provider nào được hỗ trợ.");
            return null;
        }

        if (providers.length == 1) {
            System.out.printf("🔧 Sử dụng provider: %s%n", providers[0].toUpperCase());
            return providers[0];
        }

        System.out.println("\n🏢 Chọn provider:");
        for (int i = 0; i < providers.length; i++) {
            System.out.printf("%d. %s%n", i + 1, providers[i].toUpperCase());
        }

        System.out.print("Nhập số thứ tự: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice >= 1 && choice <= providers.length) {
                return providers[choice - 1];
            } else {
                System.out.println("❌ Lựa chọn không hợp lệ.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Vui lòng nhập số hợp lệ.");
            return null;
        }
    }
}

