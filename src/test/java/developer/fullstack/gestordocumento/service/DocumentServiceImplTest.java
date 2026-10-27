package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.FirmanteUploadDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import developer.fullstack.gestordocumento.entity.TransaccionFirmaEntity;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.repository.DocumentRepository;
import developer.fullstack.gestordocumento.repository.TransaccionFirmaRepository; // <-- Importado
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransaccionFirmaRepository transaccionFirmaRepository; // <-- Nuevo mock necesario

    @InjectMocks
    private DocumentServiceImpl documentService;

    private UUID documentId;
    private UUID companyId;
    private DocumentEntity documentEntity;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Empresa Test S.A.");

        documentEntity = new DocumentEntity();
        documentEntity.setId(documentId);
        documentEntity.setOriginalName("comandos_python.pdf");
        documentEntity.setContentType("application/pdf");
        documentEntity.setSize(2048L);
        documentEntity.setUploaderEmail("faponte@yopmail.com");
        documentEntity.setStoragePath("uploads/test_file.pdf");
        documentEntity.setCompany(company);
    }

    @Test
    void findById_ShouldReturnDocumentDTO_WhenDocumentExists() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(documentEntity));
        // Simulamos la búsqueda de firmantes vacía para el mapeo a DTO
        when(transaccionFirmaRepository.findByDocumentId(documentId)).thenReturn(List.of());

        DocumentDTO result = documentService.findById(documentId);

        assertNotNull(result);
        assertEquals(documentId, result.id());
        assertEquals("comandos_python.pdf", result.fileName());
        assertEquals("faponte@yopmail.com", result.uploaderEmail());
        verify(documentRepository, times(1)).findById(documentId);
        verify(transaccionFirmaRepository, times(1)).findByDocumentId(documentId);
    }

    @Test
    void findById_ShouldThrowException_WhenDocumentNotFound() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.findById(documentId);
        });

        assertTrue(exception.getMessage().contains("Documento no encontrado"));
        verify(documentRepository, times(1)).findById(documentId);
    }

    @Test
    void findAll_ShouldReturnListOfDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(documentEntity));
        when(transaccionFirmaRepository.findByDocumentId(documentId)).thenReturn(List.of());

        List<DocumentDTO> results = documentService.findAll();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("comandos_python.pdf", results.get(0).fileName());
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void findByCompanyId_ShouldReturnList_WhenCompanyIdIsProvided() {
        Page<DocumentEntity> page = new PageImpl<>(List.of(documentEntity));
        when(documentRepository.findByCompanyId(eq(companyId), any(Pageable.class))).thenReturn(page);
        when(transaccionFirmaRepository.findByDocumentId(documentId)).thenReturn(List.of());

        List<DocumentDTO> results = documentService.findByCompanyId(companyId);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(documentRepository, times(1)).findByCompanyId(eq(companyId), any(Pageable.class));
    }

    @Test
    void findByCompanyId_ShouldReturnAll_WhenCompanyIdIsNull() {
        when(documentRepository.findAll()).thenReturn(List.of(documentEntity));
        when(transaccionFirmaRepository.findByDocumentId(documentId)).thenReturn(List.of());

        List<DocumentDTO> results = documentService.findByCompanyId(null);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void uploadWithSigners_ShouldSaveDocumentAndSigners_WhenDataIsValid() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenido de prueba".getBytes()
        );

        UserEntity user = new UserEntity();
        user.setEmail("faponte@yopmail.com");

        List<FirmanteUploadDTO> firmantes = List.of(
                new FirmanteUploadDTO("Firmante Externo", "externo@correo.com", 1)
        );

        when(userRepository.findByEmail("faponte@yopmail.com")).thenReturn(Optional.of(user));
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> {
            DocumentEntity doc = invocation.getArgument(0);
            doc.setId(UUID.randomUUID());
            return doc;
        });
        when(transaccionFirmaRepository.findByDocumentId(any())).thenReturn(List.of());

        // Ejecutamos el método que incluye los firmantes
        DocumentDTO result = documentService.uploadWithSigners(file, "faponte@yopmail.com", firmantes);

        assertNotNull(result);
        assertEquals("test.txt", result.fileName());
        
        // Verificamos que se guardó el documento y que se procesó al menos 1 transacción de firma
        verify(documentRepository, times(1)).save(any(DocumentEntity.class));
        verify(transaccionFirmaRepository, times(1)).save(any(TransaccionFirmaEntity.class));
    }

    @Test
    void upload_ShouldThrowException_WhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.upload(emptyFile, "faponte@yopmail.com");
        });

        assertEquals("No se puede almacenar un archivo vacío.", exception.getMessage());
    }

    @Test
    void findAllPaginated_ShouldFilterByCompanyAndEmail() {
        Page<DocumentEntity> page = new PageImpl<>(List.of(documentEntity));
        when(documentRepository.findByCompanyIdAndUploadedByEmail(eq(companyId), eq("faponte@yopmail.com"), any(Pageable.class)))
                .thenReturn(page);
        when(transaccionFirmaRepository.findByDocumentId(documentId)).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 10);
        PageResponseDTO<DocumentDTO> response = documentService.findAllPaginated(companyId, "faponte@yopmail.com", pageable);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        verify(documentRepository, times(1)).findByCompanyIdAndUploadedByEmail(eq(companyId), eq("faponte@yopmail.com"), any(Pageable.class));
    }

    @Test
    void download_ShouldThrowException_WhenFileNotFoundOnDisk() {
        documentEntity.setStoragePath("ruta/inexistente/archivo.pdf");
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(documentEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.download(documentId);
        });

        assertTrue(exception.getMessage().contains("Error al leer el archivo desde el disco"));
    }
}