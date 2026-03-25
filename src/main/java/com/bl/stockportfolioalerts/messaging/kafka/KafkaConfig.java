package com.bl.stockportfolioalerts.messaging.kafka;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @PostConstruct
    public void check() {
        System.out.println("KafkaConfig LOADED");
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        );
    }

    @Bean
    public NewTopic stockPriceTopic() {
        return TopicBuilder.name("stock-price")
                .partitions(1)
                .replicas(1)
                .build();
    }
}