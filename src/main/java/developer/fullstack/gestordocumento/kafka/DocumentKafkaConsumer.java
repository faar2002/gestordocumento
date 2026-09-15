package developer.fullstack.gestordocumento.kafka;

import developer.fullstack.gestordocumento.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentKafkaConsumer {

    @KafkaListener(topics = KafkaConfig.DOCUMENT_TOPIC, groupId = "document-processor-group")
    public void consume(String documentIdStr) {
        try {
            UUID documentId = UUID.fromString(documentIdStr);
            
            // AQUÍ COLOCAS LA LÁGICA PESADA (ej: escaneo de virus, conversión, 
            // validación en bases de datos externas, notificaciones, etc.)
            System.out.println("Procesando documento asíncronamente con ID: " + documentId);
            
            // Simulación de tarea en segundo plano
            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println("Error procesando el evento de documento: " + e.getMessage());
            // Aquí puedes manejar reintentos o enviar a una Dead Letter Queue (DLQ)
        }
    }
}