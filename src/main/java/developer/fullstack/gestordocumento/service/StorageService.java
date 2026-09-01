package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.DocumentResponseDTO;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StorageService {
    // Firma actualizada recibiendo el parámetro email
    DocumentResponseDTO upload(MultipartFile file, String email);
    
    Resource download(UUID id);
    DocumentEntity getMetadata(UUID id);
    List<DocumentResponseDTO> listAll();
}