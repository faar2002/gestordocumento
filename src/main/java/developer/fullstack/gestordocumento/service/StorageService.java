package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.DocumentResponseDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import developer.fullstack.gestordocumento.enums.DocumentStatus;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StorageService {
    // Firma actualizada recibiendo el parámetro email
    DocumentResponseDTO upload(MultipartFile file, String email);
    DocumentResponseDTO updateStatus(UUID id, DocumentStatus status);
    
    Resource download(UUID id);
    DocumentEntity getMetadata(UUID id);
    List<DocumentResponseDTO> listAll();

    PageResponseDTO<DocumentResponseDTO> findByEmailPaginated(String email, Pageable pageable);
}