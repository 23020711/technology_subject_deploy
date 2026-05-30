package com.pricehawl.repository;

import com.pricehawl.document.ProductDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Khi Elasticsearch không available, Spring sẽ tự động tạo implementation này
 * để tránh lỗi "No qualifying bean".
 */
@ConditionalOnMissingBean(ElasticsearchRepository.class)
@Repository
public class ProductSearchRepositoryFallback implements ProductSearchRepository {

    @Override
    public List<ProductDocument> search(String keyword) {
        return List.of();
    }

    @Override
    public <S extends ProductDocument> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends ProductDocument> Iterable<S> saveAll(Iterable<S> entities) {
        return entities;
    }

    @Override
    public Optional<ProductDocument> findById(String id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        return false;
    }

    @Override
    public Iterable<ProductDocument> findAll() {
        return List.of();
    }

    @Override
    public Iterable<ProductDocument> findAllById(Iterable<String> ids) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String id) {
    }

    @Override
    public void delete(ProductDocument entity) {
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
    }

    @Override
    public void deleteAll(Iterable<? extends ProductDocument> entities) {
    }

    @Override
    public void deleteAll() {
    }
}
