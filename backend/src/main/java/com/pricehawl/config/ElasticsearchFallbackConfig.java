package com.pricehawl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

/**
 * Cung cấp ElasticsearchOperations stub khi Elasticsearch không được bật.
 */
@Configuration
@ConditionalOnMissingBean(ElasticsearchOperations.class)
public class ElasticsearchFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(ElasticsearchConverter.class)
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchOperationsStub() {
        // Return a no-op implementation
        return new ElasticsearchOperations() {
            @Override
            public <T> T save(T entity) {
                return entity;
            }

            @Override
            public <T> Iterable<T> save(Iterable<T> entities) {
                return entities;
            }

            @Override
            public <T> T save(T entity, String indexName) {
                return entity;
            }

            @Override
            public <T> Iterable<T> save(Iterable<T> entities, String indexName) {
                return entities;
            }

            @Override
            public <T, ID> T save(T entity, Class<T> clazz) {
                return entity;
            }

            @Override
            public <T, ID> Iterable<T> save(Iterable<T> entities, Class<T> clazz) {
                return entities;
            }

            @Override
            public <T, ID> T findById(ID id, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T findById(Object id) {
                return null;
            }

            @Override
            public boolean exists(Object id) {
                return false;
            }

            @Override
            public <T> Iterable<T> findAll(Class<T> clazz) {
                return java.util.List.of();
            }

            @Override
            public <T> Iterable<T> findAll(Class<T> clazz, org.springframework.data.domain.Pageable pageable) {
                return java.util.List.of();
            }

            @Override
            public <T> Iterable<T> findAll(org.springframework.data.domain.Sort sort, Class<T> clazz) {
                return java.util.List.of();
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public <T> long count(Class<T> clazz) {
                return 0;
            }

            @Override
            public void delete(Object id) {
            }

            @Override
            public <T> void delete(T entity) {
            }

            @Override
            public <T> void delete(Class<T> clazz) {
            }

            @Override
            public void deleteAll() {
            }

            @Override
            public <T> void deleteAll(Class<T> clazz) {
            }

            @Override
            public <T> void deleteAllById(Iterable<?> ids, Class<T> clazz) {
            }

            @Override
            public <T> org.springframework.data.elasticsearch.core.SearchHits<T> search(
                    org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> org.springframework.data.elasticsearch.core.SearchHits<T> search(
                    org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz,
                    org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
                return null;
            }

            @Override
            public <T> org.springframework.data.elasticsearch.core.SearchHits<T> searchForList(
                    org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> org.springframework.data.elasticsearch.core.SearchHits<T> searchForList(
                    org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz,
                    org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
                return null;
            }

            @Override
            public <T> org.springframework.data.elasticsearch.core.SearchHits<T> count(
                    org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> org.springframework.data.elasticsearch.core.SearchHits<T> count(
                    org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz,
                    org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
                return null;
            }

            @Override
            public <T> void index(org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz) {
            }

            @Override
            public <T> void index(org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz,
                                   org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
            }

            @Override
            public <T> void delete(org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz) {
            }

            @Override
            public <T> void delete(org.springframework.data.elasticsearch.core.query.Query query, Class<T> clazz,
                                   org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
            }

            @Override
            public <T> void bulkIndex(org.springframework.data.elasticsearch.core.query.BulkQuery bulkQuery, Class<T> clazz) {
            }

            @Override
            public <T> void bulkIndex(org.springframework.data.elasticsearch.core.query.BulkQuery bulkQuery, Class<T> clazz,
                                      org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
            }

            @Override
            public <T> void bulkUpdate(org.springframework.data.elasticsearch.core.query.BulkQuery bulkQuery, Class<T> clazz) {
            }

            @Override
            public <T> void bulkUpdate(org.springframework.data.elasticsearch.core.query.BulkQuery bulkQuery, Class<T> clazz,
                                       org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
            }

            @Override
            public org.springframework.data.elasticsearch.core.ElasticsearchTemplate withoutTunnel() {
                return null;
            }

            @Override
            public <T> T update(org.springframework.data.elasticsearch.core.query.UpdateQuery query, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> T update(org.springframework.data.elasticsearch.core.query.UpdateQuery query, Class<T> clazz,
                                org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
                return null;
            }

            @Override
            public org.springframework.data.elasticsearch.core.ElasticsearchOperations withIndex(String indexName) {
                return this;
            }

            @Override
            public ElasticsearchConverter getElasticsearchConverter() {
                return null;
            }

            @Override
            public org.springframework.data.elasticsearch.core.index.IndexOps indexOps(Class<?> clazz) {
                return null;
            }

            @Override
            public org.springframework.data.elasticsearch.core.index.IndexOps indexOps(
                    org.springframework.data.elasticsearch.core.index.IndexCoordinates indexCoordinates) {
                return null;
            }

            @Override
            public <T> T get(Object id, Class<T> clazz, org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
                return null;
            }

            @Override
            public <T> T get(Object id, Class<T> clazz) {
                return null;
            }

            @Override
            public <T> Iterable<T> get(Iterable<?> ids, Class<T> clazz) {
                return java.util.List.of();
            }

            @Override
            public <T> Iterable<T> get(Iterable<?> ids, Class<T> clazz, org.springframework.data.elasticsearch.core.index.IndexCoordinates index) {
                return java.util.List.of();
            }

            @Override
            public <T> Iterable<T> multiGet(Class<T> clazz, org.springframework.data.elasticsearch.core.index.IndexCoordinates index,
                                            Collection<?> ids) {
                return java.util.List.of();
            }

            @Override
            public <T> T first(Class<T> clazz) {
                return null;
            }
        };
    }
}
