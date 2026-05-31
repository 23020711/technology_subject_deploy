# 🚨 DEPLOYMENT FIXES - KHÔNG ĐƯỢC THAY ĐỔI KHI MERGE

## Tài liệu này ghi lại TẤT CẢ các fix đã làm để deploy thành công
## MỌI merge từ repo khác phải GIỮ NGUYÊN các file bên dưới

---

## 1. SecurityConfig.java - CORS & Security

**File:** `backend/src/main/java/com/pricehawl/config/SecurityConfig.java`

### Vấn đề gốc:
- Frontend Vercel gọi `/ai-chat/stream` → Backend trả 403 Forbidden

### Fix đã làm:
- Thêm `/ai-chat/**` vào `permitAll()` trong security config
- Đảm bảo CORS cho phép origin `https://technology-subject-deploy.vercel.app`

### Code quan trọng (GIỮ NGUYÊN):
```java
// CORS allowed origins - KHÔNG ĐƯỢC SỬA
List<String> allowedOrigins = List.of(
    "https://technology-subject-deploy.vercel.app",
    "https://*.vercel.app",
    "http://localhost:*",
    "http://127.0.0.1:*"
);

// Security - THÊM /ai-chat/** vào permitAll
.requestMatchers(HttpMethod.POST,
    "/api/wishlist/**",
    "/wishlist/**",
    "/api/notifications/**",
    "/notifications/**",
    "/api/alerts/**",
    "/alerts/**",
    "/ai-chat/**"  // ← DÒNG NÀY QUAN TRỌNG
).permitAll()
```

---

## 2. AiChatPublicController.java - Endpoint mới

**File:** `backend/src/main/java/com/pricehawl/controller/AiChatPublicController.java`

### Vấn đề gốc:
- Frontend gọi `/ai-chat/stream` nhưng controller cũ là `/api/ai-chat/stream`
- Spring trả "No static resource ai-chat/stream" → 500 Error

### Fix đã làm:
- Tạo controller mới với `@RequestMapping("/ai-chat")` (không có /api)
- Endpoint: `POST /ai-chat/stream` (SSE streaming)

### Code quan trọng (GIỮ NGUYÊN):
```java
@RestController
@RequestMapping("/ai-chat")  // ← KHÔNG CÓ /api/
@RequiredArgsConstructor
public class AiChatPublicController {
    
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody AiChatRequest request) {
        // ... logic xử lý AI chat
    }
}
```

---

## 3. application.yml - Production Config

**File:** `backend/src/main/resources/application.yml`

### Cấu hình Railway/Production (GIỮ NGUYÊN):

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require}
    username: ${SPRING_DATASOURCE_USERNAME:postgres.astkanfsacxriwprspqr}
    password: ${SPRING_DATASOURCE_PASSWORD:PriceHawl123@}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      keepalive-time: 30000 
      idle-timeout: 60000
      max-lifetime: 1800000
      connection-timeout: 30000

  elasticsearch:
    uris: ${ELASTICSEARCH_URI:http://localhost:9200}
    enabled: ${ELASTICSEARCH_ENABLED:false}  # ← Elasticsearch disabled, dùng PostgreSQL fallback

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: false

ai:
  base-url: ${AI_BASE_URL:https://generativelanguage.googleapis.com}
  api-key: ${AI_API_KEY:}
  model: ${AI_MODEL:gemini-2.5-flash}
  enable-search: true
```

---

## 4. Lỗi PostgreSQL Connection (Không fix được - Railway limit)

### Vấn đề:
```
max clients reached in session mode - max clients are limited to pool_size: 15
```

### Giải pháp:
- **Restart deployment trên Railway Dashboard**
- Không thể tăng limit vì là plan miễn phí của Railway
- HikariCP đã set `maximum-pool-size: 5` để giảm thiểu vấn đề

---

## 5. ProductSearchServiceNoOp.java - Fallback Search

**File:** `backend/src/main/java/com/pricehawl/service/ProductSearchServiceNoOp.java`

### Vấn đề gốc:
- Elasticsearch disabled → search trả về []
- Cần fallback search bằng PostgreSQL

### Fix đã làm:
- Khi `spring.elasticsearch.enabled=false`, Spring tự dùng `ProductSearchServiceNoOp`
- Search sử dụng `productRepository.findByNameContainingIgnoreCase()`

### Code quan trọng (GIỮ NGUYÊN):
```java
@Service
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "false", matchIfMissing = false)
public class ProductSearchServiceNoOp implements ProductSearchServiceInterface {
    
    @Override
    public List<ProductSearchDTO> searchFallback(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword.trim())
            .stream()
            .limit(50)
            .map(this::toSearchDTO)
            .collect(Collectors.toList());
    }
}
```

---

## 📋 CHECKLIST KHI MERGE TỪ REPO KHÁC

### 🔴 TUYỆT ĐỐI KHÔNG THAY ĐỔI:
- [ ] `backend/src/main/resources/application.yml`
- [ ] `backend/src/main/java/com/pricehawl/config/SecurityConfig.java`
- [ ] `backend/src/main/java/com/pricehawl/controller/AiChatPublicController.java`

### 🟡 CÓ THỂ THAY ĐỔI (logic mới):
- [ ] Các service logic mới
- [ ] Các controller logic mới (TRỪ AiChatPublicController)
- [ ] Entity, Repository mới
- [ ] DTO mới

### 🟢 SAU KHI MERGE:
1. Chạy `git status` xem có conflict không
2. Nếu conflict ở file 🔴 → **GIỮ NGUYÊN CODE HIỆN TẠI**
3. Nếu conflict ở file 🟡 → merge bình thường
4. Push và verify Railway deploy thành công

---

## 🔗 URLs Production

- **Backend:** https://technologysubjectdeploy-production-156e.up.railway.app
- **Frontend:** https://technology-subject-deploy.vercel.app
- **Swagger:** https://technologysubjectdeploy-production-156e.up.railway.app/swagger-ui/index.html

---

## 📅 Ngày tạo: 2026-05-31
## 👤 Người tạo: AI Assistant
## 📝 Mục đích: Preserve deployment fixes across repository merges
