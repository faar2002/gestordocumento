package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
}
