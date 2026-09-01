package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.DocumentResponseDTO;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import developer.fullstack.gestordocumento.repository.DocumentRepository;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootPath;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public StorageServiceImpl(@Value("${storage.location:uploads}") String location,
                               DocumentRepository documentRepository,
                               UserRepository userRepository) {
        this.rootPath = Paths.get(location);
        this.documentRepository = documentRepository;
        this.userRepository = userRepository; // Inicialización
        try {
            Files.createDirectories(this.rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Error al crear el directorio de subidas", e);
        }
    }

    @Override
    public DocumentResponseDTO upload(MultipartFile file, String email) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No se puede guardar un archivo vacío.");
        }

        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destination = this.rootPath.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            DocumentEntity entity = new DocumentEntity();
            entity.setOriginalName(file.getOriginalFilename());
            entity.setStoragePath(destination.toString());
            entity.setContentType(file.getContentType());
            entity.setSize(file.getSize());
            
            // 1. Asigna el email recibido al documento
            entity.setEmail(email);

            // 2. Opcional: Si el email pertenece a un usuario registrado, se asocia automáticamente
            if (email != null && !email.isBlank()) {
                userRepository.findByEmail(email).ifPresent(entity::setUploadedBy);
            }

            DocumentEntity saved = documentRepository.save(entity);

            return new DocumentResponseDTO(
                saved.getId(),
                saved.getOriginalName(),
                saved.getContentType(),
                saved.getSize(),
                saved.getEmail(), // Incluye el email en la respuesta DTO
                saved.getUploadedAt()
            );
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el archivo en disco", e);
        }
    }

    @Override
    public Resource download(UUID id) {
        DocumentEntity entity = getMetadata(id);
        try {
            Path file = Paths.get(entity.getStoragePath());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("No se pudo leer el archivo en disco");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Ruta no válida", e);
        }
    }

    @Override
    public DocumentEntity getMetadata(UUID id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado con el id: " + id));
    }

    @Override
    public List<DocumentResponseDTO> listAll() {
        return documentRepository.findAll().stream()
            .map(doc -> new DocumentResponseDTO(
                doc.getId(),
                doc.getOriginalName(),
                doc.getContentType(),
                doc.getSize(),
                doc.getEmail(), // Incluye el email en la respuesta DTO
                doc.getUploadedAt()
            ))
            .toList();
    }
}