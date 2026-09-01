package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.WorkGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkGroupRepository extends JpaRepository<WorkGroupEntity, UUID> {
    boolean existsByName(String name);
    
    // Obtener los grupos que pertenecen a una empresa específica
    List<WorkGroupEntity> findByCompanyId(UUID companyId);
}