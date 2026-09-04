package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    // Búsqueda paginada por correo (busca tanto en el campo email como en la relación uploadedBy.email)
    @Query("SELECT d FROM DocumentEntity d WHERE LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')) OR (d.uploadedBy IS NOT NULL AND LOWER(d.uploadedBy.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<DocumentEntity> findByEmailPaginated(@Param("email") String email, Pageable pageable);
}
