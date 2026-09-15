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
    List<DocumentDTO> findByCompanyId(UUID companyId);

    // Método actualizado para búsqueda paginada con filtros opcionales (empresa y/o correo)
    PageResponseDTO<DocumentDTO> findAllPaginated(UUID companyId, String email, Pageable pageable);

    // Mantenemos este si requieres la consulta específica solo por correo con paginación
    PageResponseDTO<DocumentDTO> findByEmailPaginated(String email, Pageable pageable);

    byte[] download(UUID id);
}