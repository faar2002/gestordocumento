package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID; // Importante: importar UUID

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    // Consulta directa filtrando por la empresa asociada al documento
    Page<DocumentEntity> findByCompanyId(UUID companyId, Pageable pageable);

    // Consulta directa filtrando por empresa Y por correo del usuario
    Page<DocumentEntity> findByCompanyIdAndUploadedByEmail(UUID companyId, String email, Pageable pageable);

    // Consulta filtrada solo por correo
    Page<DocumentEntity> findByUploadedByEmail(String email, Pageable pageable);
}