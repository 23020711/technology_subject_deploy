package com.pricehawl.service.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class CrawlerRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(CrawlerRateLimiter.class);

    private static final Map<String, Integer> PLATFORM_LIMITS = Map.of(
        "tiki",     10,
        "hasaki",    5,
        "guardian",  5,
        "cocolux",   3,
        "watsons",   2
    );

    private static final int DEFAULT_LIMIT = 5;
    private static final int WINDOW_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;
    private final boolean redisAvailable;

    public CrawlerRateLimiter(
            @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisAvailable = redisTemplate != null;
        if (!redisAvailable) {
            log.warn("Redis not available, rate limiting disabled");
        }
    }

    public void acquire(String platform) {
        if (!redisAvailable) {
            return;
        }
        
        String key = "crawler:rate:" + platform.toLowerCase();
        int limit = PLATFORM_LIMITS.getOrDefault(platform.toLowerCase(), DEFAULT_LIMIT);

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
            }

            log.debug("Rate limit | platform={} | count={}/{}", platform, count, limit);

            if (count != null && count > limit) {
                Long ttl = redisTemplate.getExpire(key);
                long sleepMs = (ttl != null && ttl > 0) ? ttl * 1000L : WINDOW_SECONDS * 1000L;

                log.info("Rate limit reached | platform={} | count={}/{} | sleeping {}ms",
                        platform, count, limit, sleepMs);

                Thread.sleep(sleepMs);
                redisTemplate.delete(key);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limiter interrupted for platform={}", platform);
        } catch (Exception e) {
            log.warn("Rate limiter error for platform={}: {} → skipping", platform, e.getMessage());
        }
    }
}