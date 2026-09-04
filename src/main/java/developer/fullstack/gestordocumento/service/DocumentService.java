package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentDTO upload(MultipartFile file, String email);
    DocumentDTO findById(UUID id);
    List<DocumentDTO> findAll();
    
    // Método para búsqueda por correo con paginación
    PageResponseDTO<DocumentDTO> findByEmailPaginated(String email, Pageable pageable);
    
    byte[] download(UUID id);
}
