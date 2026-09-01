package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {
    
    // Método para buscar empresa por su identificador tributario (RUT / Tax ID)
    Optional<CompanyEntity> findByTaxId(String taxId);
    
    // Método para verificar si ya existe una empresa con ese nombre
    boolean existsByName(String name);
}