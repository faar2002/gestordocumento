package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.*;
import developer.fullstack.gestordocumento.repository.DocumentRepository;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    private final Path fileStorageLocation;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los archivos subidos.", ex);
        }
    }

    @Override
    public DocumentDTO upload(MultipartFile file, String email) {
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
                userRepository.findByEmail(email).ifPresent(document::setUploadedBy);
            }

            DocumentEntity savedDocument = documentRepository.save(document);
            return mapToDTO(savedDocument);

        } catch (IOException ex) {
            throw new RuntimeException("Error al almacenar el archivo " + originalFileName, ex);
        }
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
    public PageResponseDTO<DocumentDTO> findByEmailPaginated(String email, Pageable pageable) {
        Page<DocumentEntity> documentPage = documentRepository.findByEmailPaginated(email, pageable);

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

    private DocumentDTO mapToDTO(DocumentEntity entity) {
        String ownerEmail = entity.getEmail();
        if (ownerEmail == null && entity.getUploadedBy() != null) {
            ownerEmail = entity.getUploadedBy().getEmail();
        }

        return new DocumentDTO(
                entity.getId(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSize() != null ? entity.getSize() : 0L,
                entity.getUploadedAt(),
                ownerEmail
        );
    }
}