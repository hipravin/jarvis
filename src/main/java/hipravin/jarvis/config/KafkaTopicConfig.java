package hipravin.jarvis.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaTopicConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    NewTopic clockSecondsTopic() {
        return TopicBuilder.name("search-stat-topic")
                .partitions(10)
                .build();
    }
}
