package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.DocumentResponseDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.*;
import developer.fullstack.gestordocumento.enums.DocumentStatus;
import developer.fullstack.gestordocumento.repository.DocumentRepository;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final Path rootLocation;

    public StorageServiceImpl(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento de archivos", e);
        }
    }

    @Override
    public DocumentResponseDTO upload(MultipartFile file, String email) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No se puede guardar un archivo vacío.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed_file";
        String storedFileName = UUID.randomUUID().toString() + "_" + originalName;

        try {
            Path destinationFile = this.rootLocation.resolve(storedFileName).normalize();
            
            // Protección contra travesía de directorios (Directory Traversal Attack)
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new RuntimeException("No se puede almacenar el archivo fuera del directorio configurado.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            DocumentEntity entity = new DocumentEntity();
            entity.setOriginalName(originalName);
            entity.setStoragePath(destinationFile.toString());
            entity.setContentType(file.getContentType());
            entity.setSize(file.getSize());
            entity.setEmail(email);

            if (email != null && !email.isBlank()) {
                userRepository.findByEmail(email).ifPresent(entity::setUploadedBy);
            }

            DocumentEntity saved = documentRepository.save(entity);
            return mapToDTO(saved);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + originalName, e);
        }
    }

    @Override
    public List<DocumentResponseDTO> listAll() {
        return documentRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public PageResponseDTO<DocumentResponseDTO> findByEmailPaginated(String email, Pageable pageable) {
        Page<DocumentEntity> page = documentRepository.findByEmailPaginated(email, pageable);

        List<DocumentResponseDTO> content = page.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public Resource download(UUID id) {
        DocumentEntity entity = getMetadata(id);
        try {
            Path file = Paths.get(entity.getStoragePath());
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se puede leer el archivo en el sistema de almacenamiento.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error en la ruta del archivo solicitado.", e);
        }
    }

    @Override
    public DocumentEntity getMetadata(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con el ID: " + id));
    }

    @Override
    public DocumentResponseDTO updateStatus(UUID id, DocumentStatus status) {
        DocumentEntity entity = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con el ID: " + id));

        entity.setStatus(status);
        DocumentEntity updated = documentRepository.save(entity);
        return mapToDTO(updated);
    }
    
    private DocumentResponseDTO mapToDTO(DocumentEntity entity) {
        String ownerEmail = entity.getEmail();
        if (ownerEmail == null && entity.getUploadedBy() != null) {
            ownerEmail = entity.getUploadedBy().getEmail();
        }

        return new DocumentResponseDTO(
                entity.getId(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSize() != null ? entity.getSize() : 0L,
                entity.getUploadedAt(),
                ownerEmail,
                entity.getStatus()
        );
    }
}