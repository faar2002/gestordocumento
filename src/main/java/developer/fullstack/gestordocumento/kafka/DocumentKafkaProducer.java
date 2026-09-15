package developer.fullstack.gestordocumento.kafka;

import developer.fullstack.gestordocumento.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DocumentKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDocumentEvent(UUID documentId) {
        kafkaTemplate.send(KafkaConfig.DOCUMENT_TOPIC, documentId.toString());
    }
}