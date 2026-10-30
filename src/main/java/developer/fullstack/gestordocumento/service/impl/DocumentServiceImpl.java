package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.FirmanteResponseDTO; // <-- DTO para la respuesta de firmantes
import developer.fullstack.gestordocumento.dto.FirmanteUploadDTO; 
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.*;
import developer.fullstack.gestordocumento.repository.DocumentRepository;
import developer.fullstack.gestordocumento.repository.TransaccionFirmaRepository; 
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final TransaccionFirmaRepository transaccionFirmaRepository; 
    private final Path fileStorageLocation;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            TransaccionFirmaRepository transaccionFirmaRepository, 
            @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.transaccionFirmaRepository = transaccionFirmaRepository;
        
        String safeDir = (uploadDir != null && !uploadDir.isBlank()) ? uploadDir : "uploads";
        this.fileStorageLocation = Paths.get(safeDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los archivos subidos.", ex);
        }
    }

    /**
     * Método principal de subida adaptado para recibir y registrar los firmantes,
     * permitiendo correos que no estén necesariamente registrados en la tabla users.
     */
    @Override // Asegúrate de agregar este método en la interfaz DocumentService
    @Transactional
    public DocumentDTO uploadWithSigners(MultipartFile file, String email, List<FirmanteUploadDTO> firmantes) {
        if (file.isEmpty()) {
            throw new RuntimeException("No se puede almacenar un archivo vacío.");
        }

        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed_file";
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            DocumentEntity document = new DocumentEntity();
            document.setOriginalName(originalFileName);
            document.setStoragePath(targetLocation.toString());
            document.setContentType(file.getContentType());
            document.setSize(file.getSize());
            document.setEmail(email);

            if (email != null && !email.isBlank()) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    document.setUploadedBy(user);
                    if (user.getCompany() != null) {
                        document.setCompany(user.getCompany());
                    }
                });
            }

            // Guardamos el documento principal primero para obtener su ID
            DocumentEntity savedDocument = documentRepository.save(document);

            // Procesar y registrar los firmantes (incluso si no están registrados en userRepository)
            if (firmantes != null && !firmantes.isEmpty()) {
                for (FirmanteUploadDTO fDto : firmantes) {
                    TransaccionFirmaEntity transaccion = new TransaccionFirmaEntity();
                    transaccion.setDocument(savedDocument); 
                    transaccion.setEmailFirmante(fDto.email()); 
                    transaccion.setNombreFirmante(fDto.nombre()); 
                    transaccion.setOrden(fDto.orden());
                    transaccion.setEstado("PENDIENTE");

                    transaccionFirmaRepository.save(transaccion);
                }
            }

            return mapToDTO(savedDocument);

        } catch (IOException ex) {
            throw new RuntimeException("Error al almacenar el archivo " + originalFileName, ex);
        }
    }

    // Método original mantenido por compatibilidad
    @Override
    public DocumentDTO upload(MultipartFile file, String email) {
        return uploadWithSigners(file, email, null);
    }

    @Override
    public DocumentDTO findById(UUID id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con el ID: " + id));
        return mapToDTO(document);
    }

    @Override
    public List<DocumentDTO> findAll() {
        return documentRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<DocumentDTO> findByCompanyId(UUID companyId) {
        if (companyId == null) {
            return findAll();
        }
        
        return documentRepository.findByCompanyId(companyId, Pageable.unpaged())
            .getContent()
            .stream()
            .map(this::mapToDTO)
            .toList();
    }

    @Override
    public PageResponseDTO<DocumentDTO> findAllPaginated(UUID companyId, String email, Pageable pageable) {
        Page<DocumentEntity> documentPage;

        boolean hasCompany = companyId != null;
        boolean hasEmail = email != null && !email.isBlank();

        if (hasCompany && hasEmail) {
            documentPage = documentRepository.findByCompanyIdAndUploadedByEmail(companyId, email.trim(), pageable);
        } else if (hasCompany) {
            documentPage = documentRepository.findByCompanyId(companyId, pageable);
        } else if (hasEmail) {
            documentPage = documentRepository.findByUploadedByEmail(email.trim(), pageable);
        } else {
            documentPage = documentRepository.findAll(pageable);
        }

        return buildPageResponse(documentPage);
    }

    @Override
    public PageResponseDTO<DocumentDTO> findByEmailPaginated(String email, Pageable pageable) {
        return findAllPaginated(null, email, pageable);
    }

    @Override
    public byte[] download(UUID id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con el ID: " + id));

        try {
            Path filePath = Paths.get(document.getStoragePath());
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Error al leer el archivo desde el disco.", ex);
        }
    }

    private PageResponseDTO<DocumentDTO> buildPageResponse(Page<DocumentEntity> documentPage) {
        List<DocumentDTO> content = documentPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                documentPage.getNumber(),
                documentPage.getSize(),
                documentPage.getTotalElements(),
                documentPage.getTotalPages(),
                documentPage.isLast()
        );
    }

    private DocumentDTO mapToDTO(DocumentEntity entity) {
        String ownerEmail = entity.getEmail();
        if (ownerEmail == null && entity.getUploadedBy() != null) {
            ownerEmail = entity.getUploadedBy().getEmail();
        }

        // Consultamos y mapeamos los firmantes asociados a este documento
        List<FirmanteResponseDTO> firmantesDTO = transaccionFirmaRepository.findByDocumentId(entity.getId())
                .stream()
                .map(f -> new FirmanteResponseDTO(
                        f.getUuid(),
                        f.getNombreFirmante(),
                        f.getEmailFirmante(),
                        f.getOrden(),
                        f.getEstado()
                ))
                .toList();

        return new DocumentDTO(
                entity.getId(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSize() != null ? entity.getSize() : 0L,
                entity.getUploadedAt(),
                entity.getUploaderEmail(),
                ownerEmail,
                entity.getStatus() != null ? entity.getStatus().name() : "PENDIENTE",
                entity.getCompany() != null ? entity.getCompany().getId().toString() : null,
                firmantesDTO // <-- Incluye la lista de firmantes en la respuesta
        );
    }

    @Override
    public List<FirmanteResponseDTO> obtenerFirmantesPorDocumento(UUID documentId) {
        // Validamos primero que el documento exista (opcional pero recomendado)
        if (!documentRepository.existsById(documentId)) {
            throw new RuntimeException("Documento no encontrado con el ID: " + documentId);
        }

        // Buscamos las transacciones de firma y las mapeamos a DTO
        return transaccionFirmaRepository.findByDocumentId(documentId)
                .stream()
                .map(f -> new FirmanteResponseDTO(
                        f.getUuid(),
                        f.getNombreFirmante(),
                        f.getEmailFirmante(),
                        f.getOrden(),
                        f.getEstado()
                ))
                .toList();
    }
}