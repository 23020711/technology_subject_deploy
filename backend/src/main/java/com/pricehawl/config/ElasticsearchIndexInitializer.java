package com.pricehawl.config;

import com.pricehawl.repository.ProductSearchRepository;
import com.pricehawl.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

/**
 * Automatically indexes all products into Elasticsearch on application startup.
 * Only runs when spring.elasticsearch.enabled=true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ProductSearchService productSearchService;
    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) {
        log.info("ElasticsearchIndexInitializer: Starting product indexing on startup...");

        try {
            long start = System.currentTimeMillis();

            // Run the sync
            productSearchService.syncAll();

            long elapsed = System.currentTimeMillis() - start;
            long docCount = productSearchRepository.count();

            log.info("ElasticsearchIndexInitializer: Indexed {} products into Elasticsearch in {} ms",
                    docCount, elapsed);

        } catch (Exception e) {
            log.error("ElasticsearchIndexInitializer: Failed to index products on startup", e);
        }
    }
}
