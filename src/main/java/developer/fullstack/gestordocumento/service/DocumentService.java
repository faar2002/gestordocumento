package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.FirmanteUploadDTO; // <-- Asegúrate de importar esto
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    
    // Método original existente
    DocumentDTO upload(MultipartFile file, String email);
    
    // --> AGREGAR ESTA LÍNEA para que coincida con la implementación de DocumentServiceImpl
    DocumentDTO uploadWithSigners(MultipartFile file, String email, List<FirmanteUploadDTO> firmantes);

    DocumentDTO findById(UUID id);
    List<DocumentDTO> findAll();
    List<DocumentDTO> findByCompanyId(UUID companyId);
    PageResponseDTO<DocumentDTO> findAllPaginated(UUID companyId, String email, Pageable pageable);
    PageResponseDTO<DocumentDTO> findByEmailPaginated(String email, Pageable pageable);
    byte[] download(UUID id);
}