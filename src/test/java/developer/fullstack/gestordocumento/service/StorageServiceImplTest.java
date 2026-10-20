package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.DocumentResponseDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.enums.DocumentStatus;
import developer.fullstack.gestordocumento.repository.DocumentRepository;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.impl.StorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    private StorageServiceImpl storageService;

    private UUID documentId;
    private DocumentEntity documentEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Inicializamos manualmente el servicio pasando un directorio por defecto ("uploads") 
        // para evitar el NullPointerException con la anotación @Value de Spring en tests unitarios.
        storageService = new StorageServiceImpl(documentRepository, userRepository, "uploads");

        documentId = UUID.randomUUID();

        userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setEmail("faponte@yopmail.com");

        documentEntity = new DocumentEntity();
        documentEntity.setId(documentId);
        documentEntity.setOriginalName("reporte.pdf");
        documentEntity.setContentType("application/pdf");
        documentEntity.setSize(1024L);
        documentEntity.setEmail("faponte@yopmail.com");
        documentEntity.setStoragePath("uploads/test_file.pdf");
        documentEntity.setStatus(DocumentStatus.PENDIENTE);
        documentEntity.setUploadedBy(userEntity);
    }

    @Test
    void upload_ShouldSaveAndReturnDTO_WhenFileIsValid() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Contenido PDF".getBytes()
        );

        when(userRepository.findByEmail("faponte@yopmail.com")).thenReturn(Optional.of(userEntity));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> {
            DocumentEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        DocumentResponseDTO result = storageService.upload(file, "faponte@yopmail.com");

        assertNotNull(result);
        assertEquals("test.pdf", result.originalName());
        assertEquals("application/pdf", result.contentType());
        verify(documentRepository, times(1)).save(any(DocumentEntity.class));
    }

    @Test
    void upload_ShouldThrowException_WhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storageService.upload(emptyFile, "faponte@yopmail.com");
        });

        assertEquals("No se puede guardar un archivo vacío.", exception.getMessage());
        verify(documentRepository, never()).save(any());
    }

    @Test
    void listAll_ShouldReturnListOfDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(documentEntity));

        List<DocumentResponseDTO> results = storageService.listAll();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("reporte.pdf", results.get(0).originalName());
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void findByEmailPaginated_ShouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DocumentEntity> page = new PageImpl<>(List.of(documentEntity), pageable, 1);

        when(documentRepository.findByUploadedByEmail("faponte@yopmail.com", pageable)).thenReturn(page);

        PageResponseDTO<DocumentResponseDTO> response = storageService.findByEmailPaginated("faponte@yopmail.com", pageable);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals(0, response.pageNumber());
        assertEquals(1, response.totalElements());
        verify(documentRepository, times(1)).findByUploadedByEmail("faponte@yopmail.com", pageable);
    }

    @Test
    void getMetadata_ShouldReturnEntity_WhenExists() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(documentEntity));

        DocumentEntity result = storageService.getMetadata(documentId);

        assertNotNull(result);
        assertEquals(documentId, result.getId());
        assertEquals("reporte.pdf", result.getOriginalName());
        verify(documentRepository, times(1)).findById(documentId);
    }

    @Test
    void getMetadata_ShouldThrowException_WhenNotFound() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            storageService.getMetadata(documentId);
        });

        assertTrue(exception.getMessage().contains("Documento no encontrado con el ID"));
        verify(documentRepository, times(1)).findById(documentId);
    }

    @Test
    void updateStatus_ShouldUpdateAndReturnDTO_WhenExists() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(documentEntity));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponseDTO result = storageService.updateStatus(documentId, DocumentStatus.APROBADO);

        assertNotNull(result);
        assertEquals(DocumentStatus.APROBADO, result.status());
        verify(documentRepository, times(1)).findById(documentId);
        verify(documentRepository, times(1)).save(any(DocumentEntity.class));
    }
}