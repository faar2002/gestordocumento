package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.WorkGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkGroupRepository extends JpaRepository<WorkGroupEntity, UUID> {

    // Comprueba si existe un grupo con el mismo nombre dentro de la misma empresa
    boolean existsByNameAndCompanyId(String name, UUID companyId);

    // Comprueba si existe un grupo con el mismo nombre pero con un ID distinto (útil para actualización)
    boolean existsByNameAndCompanyIdAndIdNot(String name, UUID companyId, UUID id);

    List<WorkGroupEntity> findByCompanyId(UUID companyId);
}