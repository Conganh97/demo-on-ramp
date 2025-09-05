# OnRamp Integration System - Demo Application

## Giới thiệu

Ứng dụng demo này minh họa cách sử dụng OnRamp Integration System để tích hợp các dịch vụ fiat-to-crypto vào ứng dụng của bạn. Demo cung cấp giao diện command-line đơn giản để test các chức năng chính của hệ thống.

## Chức năng Demo

### 1. Xem danh sách Providers
- Hiển thị tất cả providers được hỗ trợ
- Kiểm tra trạng thái hỗ trợ của từng provider

### 2. Lấy báo giá
- Nhập cặp tiền tệ (fiat/crypto) và số tiền
- Lấy báo giá từ provider được chọn
- Hiển thị tỷ giá, phí, và tổng số tiền

### 3. Tạo đơn hàng
- Tạo đơn hàng mua tiền điện tử
- Nhập thông tin địa chỉ ví và callback URL
- Nhận Order ID để tracking

### 4. Kiểm tra trạng thái đơn hàng
- Tra cứu trạng thái đơn hàng bằng Order ID
- Hiển thị thông tin cập nhật của đơn hàng

### 5. Xem tài sản được hỗ trợ
- Liệt kê các loại tiền tệ (fiat và crypto) được hỗ trợ
- Hiển thị thông tin chi tiết về từng tài sản

### 6. Xem phương thức thanh toán
- Lấy danh sách phương thức thanh toán cho cặp tiền tệ
- Hiển thị giới hạn và thời gian xử lý

### 7. Test kết nối Provider
- Kiểm tra cấu hình và kết nối đến provider
- Validate tính khả dụng của service

## Cài đặt và chạy

### Yêu cầu
- Java 17+
- Maven 3.6+
- API keys cho các providers (Onramper, etc.)

### Bước 1: Cấu hình Environment Variables

Tạo file `.env` trong thư mục root:

```bash
# Onramper Configuration
ONRAMPER_API_KEY=your_onramper_api_key_here
ONRAMPER_API_SECRET=your_onramper_secret_here
```

**Lưu ý**: Để test demo, bạn có thể sử dụng Onramper staging environment với API keys demo.

### Bước 2: Build Project

```bash
# Từ thư mục root của project
mvn clean compile
```

### Bước 3: Chạy Demo

```bash
# Chạy demo application
mvn spring-boot:run -Dspring-boot.run.main-class=com.onramp.integration.demo.OnRampDemo -Dspring-boot.run.profiles=demo
```

Hoặc nếu đã build JAR:

```bash
java -jar target/onramp-integration-system-*.jar --spring.profiles.active=demo --spring.main.class=com.onramp.integration.demo.OnRampDemo
```

## Hướng dẫn sử dụng Demo

### Giao diện chính

Khi chạy demo, bạn sẽ thấy menu chính:

```
🚀 OnRamp Integration System Demo
============================================================

📋 MENU CHÍNH
1. Xem danh sách providers
2. Lấy báo giá
3. Tạo đơn hàng
4. Kiểm tra trạng thái đơn hàng
5. Xem tài sản được hỗ trợ
6. Xem phương thức thanh toán
7. Test kết nối provider
0. Thoát

Chọn tùy chọn (0-7):
```

### Ví dụ sử dụng

#### 1. Lấy báo giá BTC bằng USD

```
Chọn tùy chọn: 2
💰 LẤY BÁO GIÁ
------------------------------
🔧 Sử dụng provider: ONRAMPER
Nhập mã tiền pháp định (VD: USD): USD
Nhập mã tiền điện tử (VD: BTC): BTC
Nhập số tiền pháp định (VD: 100): 100

⏳ Đang lấy báo giá...

✅ BÁO GIÁ THÀNH CÔNG
------------------------------
Provider: onramper
Tiền pháp định: USD 100.00
Tiền điện tử: BTC 0.00234000
Tỷ giá: 42735.50 USD/BTC
Phí: 2.50 USD
Tổng cộng: 102.50 USD
```

#### 2. Tạo đơn hàng

```
Chọn tùy chọn: 3
🛒 TẠO ĐƠN HÀNG
------------------------------
🔧 Sử dụng provider: ONRAMPER
Nhập mã tiền pháp định (VD: USD): USD
Nhập mã tiền điện tử (VD: BTC): BTC
Nhập số tiền pháp định (VD: 100): 100
Nhập địa chỉ ví: 1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
Nhập URL callback: https://yoursite.com/callback

⏳ Đang tạo đơn hàng...

✅ TẠO ĐƠN HÀNG THÀNH CÔNG
----------------------------------------
Order ID: ord_12345678-1234-1234-1234-123456789012
External Order ID: onramper_ext_98765
Provider: onramper
Trạng thái: PENDING_PAYMENT
Tiền pháp định: USD 100.00
Tiền điện tử: BTC 0.00234000
Địa chỉ ví: 1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
Thời gian tạo: 2025-09-04T16:30:00

💡 Lưu Order ID để kiểm tra trạng thái sau: ord_12345678-1234-1234-1234-123456789012
```

#### 3. Kiểm tra trạng thái đơn hàng

```
Chọn tùy chọn: 4
🔍 KIỂM TRA TRẠNG THÁI ĐƠN HÀNG
----------------------------------------
🔧 Sử dụng provider: ONRAMPER
Nhập Order ID: ord_12345678-1234-1234-1234-123456789012

⏳ Đang kiểm tra trạng thái...

✅ TRẠNG THÁI ĐƠN HÀNG
------------------------------
Order ID: ord_12345678-1234-1234-1234-123456789012
Trạng thái: PROCESSING
Provider: onramper
Cập nhật lần cuối: 2025-09-04T16:35:00

⚙️ Đơn hàng đang được xử lý.
```

## Cấu hình Demo

### File cấu hình: `demo/application-demo.yml`

```yaml
onramp:
  providers:
    onramper:
      api-key: ${ONRAMPER_API_KEY:demo-api-key}
      api-secret: ${ONRAMPER_API_SECRET:demo-secret}
      base-url: https://api-stg.onramper.com
      is-sandbox: true
      timeout: 30
      retry-attempts: 3
      enabled: true

demo:
  default-fiat-currency: USD
  default-crypto-currency: BTC
  default-amount: 100.0
  default-wallet-address: 1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
  default-redirect-url: https://demo.onramp-integration.com/callback
```

### Tùy chỉnh cấu hình

Bạn có thể tùy chỉnh các giá trị mặc định trong file cấu hình:

- `default-fiat-currency`: Tiền pháp định mặc định
- `default-crypto-currency`: Tiền điện tử mặc định  
- `default-amount`: Số tiền mặc định
- `default-wallet-address`: Địa chỉ ví mặc định
- `default-redirect-url`: URL callback mặc định

## Troubleshooting

### Lỗi thường gặp

#### 1. API Key không hợp lệ

```
❌ Lỗi khi lấy báo giá: Invalid configuration: API key cannot be null or empty
```

**Giải pháp**: Kiểm tra environment variables `ONRAMPER_API_KEY` và `ONRAMPER_API_SECRET`.

#### 2. Kết nối timeout

```
❌ Lỗi khi lấy báo giá: Connection timeout to Onramper
```

**Giải pháp**: 
- Kiểm tra kết nối internet
- Tăng timeout trong cấu hình
- Thử lại sau vài phút

#### 3. Provider không được hỗ trợ

```
❌ Lỗi: Provider not supported: invalid-provider
```

**Giải pháp**: Sử dụng tùy chọn 1 để xem danh sách providers được hỗ trợ.

#### 4. Tham số không hợp lệ

```
❌ Lỗi khi lấy báo giá: Invalid currency format
```

**Giải pháp**: 
- Sử dụng mã tiền tệ chuẩn (USD, EUR, BTC, ETH, etc.)
- Kiểm tra số tiền phải là số dương
- Đảm bảo địa chỉ ví hợp lệ

### Debug Mode

Để bật debug mode, thêm vào cấu hình:

```yaml
logging:
  level:
    com.onramp.integration: DEBUG
    org.springframework.web.reactive: DEBUG
```

Hoặc chạy với parameter:

```bash
java -jar target/onramp-integration-system-*.jar --logging.level.com.onramp.integration=DEBUG
```

## Mở rộng Demo

### Thêm Provider mới

1. Implement provider service theo hướng dẫn trong [Developer Guide](../docs/DEVELOPER_GUIDE.md)
2. Đăng ký provider trong factory
3. Thêm cấu hình vào `application-demo.yml`
4. Provider sẽ tự động xuất hiện trong menu demo

### Thêm chức năng mới

1. Thêm menu option mới trong `showMainMenu()`
2. Implement method xử lý tương ứng
3. Sử dụng các APIs có sẵn từ `OnRampService`

### Tạo GUI Demo

Demo hiện tại sử dụng command-line interface. Bạn có thể tạo GUI demo bằng:

- **Spring Boot Web**: Tạo REST APIs và web interface
- **JavaFX**: Desktop application với GUI
- **React/Angular**: Frontend riêng biệt gọi APIs

## Kết luận

Demo application này cung cấp một cách đơn giản để:

- Hiểu cách hoạt động của OnRamp Integration System
- Test các chức năng chính
- Làm quen với APIs và data models
- Prototype integration vào ứng dụng thực tế

Để tích hợp vào production, tham khảo:
- [Developer Guide](../docs/DEVELOPER_GUIDE.md)
- [API Reference](../docs/API_REFERENCE.md)
- [Architecture Documentation](../docs/ARCHITECTURE.md)

---

**Tác giả**: Manus AI  
**Phiên bản**: 1.0.0  
**Cập nhật lần cuối**: 2025-09-04

