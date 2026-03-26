package com.bl.stockportfolioalerts.messaging.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class KafkaHealthConfig {

    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        return () -> {
            try (AdminClient adminClient = AdminClient.create(
                    Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"))) {

                adminClient.listTopics().names().get();

                return Health.up().withDetail("kafka", "Available").build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
}
