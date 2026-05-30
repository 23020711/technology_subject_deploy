package com.pricehawl.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration để disable Elasticsearch khi không có server.
 */
@Configuration
public class ElasticsearchConfig {
    // Elasticsearch được disabled bằng application.yml
    // spring.elasticsearch.enabled=false
}
