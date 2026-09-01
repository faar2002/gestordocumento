package developer.fullstack.gestordocumento.repository;

import developer.fullstack.gestordocumento.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    
    // Método clave para buscar usuario por su email y asociarlo a la subida de documentos
    Optional<UserEntity> findByEmail(String email);
    
    // Método para verificar si el correo ya está registrado
    boolean existsByEmail(String email);
    
    // Buscar todos los usuarios pertenecientes a una empresa específica
    List<UserEntity> findByCompanyId(UUID companyId);
}