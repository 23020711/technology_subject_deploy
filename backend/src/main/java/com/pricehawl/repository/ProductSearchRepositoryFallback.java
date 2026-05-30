package com.pricehawl.repository;

import com.pricehawl.document.ProductDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation khi Elasticsearch không khả dụng.
 */
@Component
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "false", matchIfMissing = false)
public class ProductSearchRepositoryFallback {

    public List<ProductDocument> search(String keyword) {
        return Collections.emptyList();
    }

    public void deleteAll() {
    }

    public <S extends ProductDocument> Iterable<S> saveAll(Iterable<S> entities) {
        return entities;
    }

    public <S extends ProductDocument> S save(S entity) {
        return entity;
    }
}
