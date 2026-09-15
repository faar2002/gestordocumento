package developer.fullstack.gestordocumento.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String DOCUMENT_TOPIC = "document-upload-topic";

    @Bean
    public NewTopic documentTopic() {
        return TopicBuilder.name(DOCUMENT_TOPIC)
                .partitions(3) // Permite paralelizar el consumo con múltiples instancias
                .replicas(1)
                .build();
    }
}