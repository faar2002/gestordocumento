package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.TransaccionFirmaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // <-- Importación necesaria para el Optional
import java.util.UUID;

@Repository
public interface TransaccionFirmaRepository extends JpaRepository<TransaccionFirmaEntity, UUID> {
    
    // Método para buscar una transacción individual por su UUID único
    Optional<TransaccionFirmaEntity> findByUuid(UUID uuid);

    // Retorna una lista de transacciones asociadas a un documento específico
    List<TransaccionFirmaEntity> findByDocumentId(UUID documentId);
}