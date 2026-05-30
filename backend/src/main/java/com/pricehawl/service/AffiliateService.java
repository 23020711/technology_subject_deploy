package com.pricehawl.service;

import com.pricehawl.entity.AffiliateClick;
import com.pricehawl.repository.AffiliateClickRepository;
import com.pricehawl.repository.ProductListingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AffiliateService {

    private static final String PUBLISHER_ID = "6964549063767105843";

    private static final java.util.Map<String, String> CAMPAIGN_IDS = java.util.Map.of(
        "watsons", "5701062214502283005",
        "tiki",    "4348614231480407268"
    );

    private static final java.util.List<String> BOT_AGENTS = java.util.List.of(
        "googlebot", "bingbot", "yandex", "baiduspider", "facebookexternalhit"
    );

    private final AffiliateClickRepository clickRepository;
    private final ProductListingRepository listingRepository;
    private final StringRedisTemplate redisTemplate;
    private final boolean redisAvailable;
    
    // Fallback in-memory cache when Redis is not available
    private final ConcurrentHashMap<String, Long> inMemoryCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> inMemoryExpiry = new ConcurrentHashMap<>();

    public AffiliateService(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            AffiliateClickRepository clickRepository,
            ProductListingRepository listingRepository) {
        this.redisTemplate = redisTemplate;
        this.redisAvailable = redisTemplate != null;
        this.clickRepository = clickRepository;
        this.listingRepository = listingRepository;
        if (!redisAvailable) {
            log.warn("Redis not available, using in-memory spam protection");
        }
    }

    public String processClick(UUID productId, String platform,
                               String userId, String ip, String userAgent) {

        if (isBot(userAgent)) {
            log.debug("Bot detected, skipping: {}", userAgent);
            return buildAffiliateUrl(productId, platform, null);
        }

        String clickId = UUID.randomUUID().toString();

        String spamKey1 = "aff:spam1:" + ip + ":" + productId;
        Boolean firstClick;
        
        if (redisAvailable) {
            firstClick = redisTemplate.opsForValue()
                .setIfAbsent(spamKey1, clickId, 5, TimeUnit.MINUTES);
        } else {
            firstClick = inMemorySetIfAbsent(spamKey1, clickId, 5, TimeUnit.MINUTES);
        }

        String spamKey2 = "aff:spam2:" + ip;
        Long clickCount;
        
        if (redisAvailable) {
            clickCount = redisTemplate.opsForValue().increment(spamKey2);
            if (clickCount != null && clickCount == 1) {
                redisTemplate.expire(spamKey2, 1, TimeUnit.MINUTES);
            }
        } else {
            clickCount = inMemoryIncrement(spamKey2);
        }
        
        boolean suspicious = clickCount != null && clickCount > 10;

        if (Boolean.TRUE.equals(firstClick) && !suspicious) {
            UUID userUuid = null;
            try { userUuid = userId != null ? UUID.fromString(userId) : null; } catch (Exception ignored) {}
            saveClickAsync(userUuid, productId, platform, clickId, ip, userAgent);
        } else {
            log.debug("Spam click filtered: ip={}, product={}, count={}", ip, productId, clickCount);
        }

        return buildAffiliateUrl(productId, platform, clickId);
    }
    
    private boolean inMemorySetIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        long now = System.currentTimeMillis();
        long deadline = now + unit.toMillis(timeout);
        
        Long existingExpiry = inMemoryExpiry.get(key);
        if (existingExpiry != null && existingExpiry > now) {
            return false;
        }
        
        inMemoryCache.put(key, deadline);
        inMemoryExpiry.put(key, deadline);
        return true;
    }
    
    private Long inMemoryIncrement(String key) {
        long now = System.currentTimeMillis();
        String countKey = key + ":count";
        
        Long current = inMemoryCache.get(countKey);
        if (current == null) {
            inMemoryCache.put(countKey, 1L);
            inMemoryExpiry.put(key, now + TimeUnit.MINUTES.toMillis(1));
            return 1L;
        }
        inMemoryCache.put(countKey, current + 1);
        return current + 1;
    }

    @Async
    public void saveClickAsync(UUID userId, UUID productId, String platform,
                               String clickId, String ip, String userAgent) {
        try {
            AffiliateClick click = AffiliateClick.builder()
                .userId(userId)
                .productId(productId)
                .platform(platform)
                .clickId(clickId)
                .ip(ip)
                .userAgent(userAgent != null && userAgent.length() > 255
                    ? userAgent.substring(0, 255) : userAgent)
                .build();
            clickRepository.save(click);
        } catch (Exception e) {
            log.error("Failed to save affiliate click: {}", e.getMessage());
        }
    }

    private String buildAffiliateUrl(UUID productId, String platform, String clickId) {
        String productUrl = listingRepository
            .findByProductIdAndPlatformNameIgnoreCase(productId, platform)
            .stream()
            .findFirst()
            .map(l -> l.getUrl())
            .orElse(null);

        if (productUrl == null) {
            log.warn("No listing found for productId={}, platform={}", productId, platform);
            return "https://pricehawk.vn";
        }

        String platformKey = platform.toLowerCase();
        String campaignId = CAMPAIGN_IDS.get(platformKey);

        if (campaignId == null) return productUrl;

        String encoded = Base64.getEncoder().encodeToString(productUrl.getBytes())
            .replace("+", "%2B")
            .replace("=", "%3D")
            .replace("/", "%2F");

        String url = String.format(
            "https://go.isclix.com/deep_link/v5/%s/%s?sub4=pricehawk&url_enc=%s",
            PUBLISHER_ID, campaignId, encoded
        );

        if (clickId != null) {
            url += "&sub1=" + clickId;
        }

        return url;
    }

    private boolean isBot(String userAgent) {
        if (userAgent == null) return false;
        String lower = userAgent.toLowerCase();
        return BOT_AGENTS.stream().anyMatch(lower::contains);
    }
}