package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.SystemAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SystemAccessRepository extends JpaRepository<SystemAccessEntity, UUID> {
    
    // Método para buscar un sistema por su código único (ej: "CRM", "ERP")
    Optional<SystemAccessEntity> findBySystemCode(String systemCode);
    
    // Método para verificar existencia por código
    boolean existsBySystemCode(String systemCode);
}