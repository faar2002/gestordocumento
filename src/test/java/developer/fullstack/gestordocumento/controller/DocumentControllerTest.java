package developer.fullstack.gestordocumento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser; // <-- Importante
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@WithMockUser(username = "admin@empresa.com", roles = {"ADMIN"}) // <-- Salta la seguridad simulando un usuario autenticado
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    private UUID documentId;
    private UUID companyId;
    private DocumentDTO documentDTO;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        documentDTO = new DocumentDTO(
                documentId,
                "contrato.pdf",
                "application/pdf",
                2048L,
                LocalDateTime.now(),
                "admin@empresa.com",
                "propietario@empresa.com",
                "ACTIVE",
                companyId.toString()
        );
    }

    @Test
    void uploadFile_ShouldReturnCreated_WhenFileIsValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contrato.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "contenido de prueba".getBytes()
        );

        when(documentService.upload(any(), eq("admin@empresa.com"))).thenReturn(documentDTO);

        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("email", "admin@empresa.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(documentId.toString()))
                .andExpect(jsonPath("$.fileName").value("contrato.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"));

        verify(documentService, times(1)).upload(any(), eq("admin@empresa.com"));
    }

    @Test
    void searchDocuments_ShouldReturnPageResponse_WhenParametersAreValid() throws Exception {
        PageResponseDTO<DocumentDTO> pageResponse = new PageResponseDTO<>(
                List.of(documentDTO), 0, 10, 1L, 1, true
        );

        when(documentService.findAllPaginated(eq(companyId), eq("admin@empresa.com"), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/documents/search")
                        .param("companyId", companyId.toString())
                        .param("email", "admin@empresa.com")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "uploadedAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(documentId.toString()))
                .andExpect(jsonPath("$.content[0].fileName").value("contrato.pdf"));

        verify(documentService, times(1)).findAllPaginated(eq(companyId), eq("admin@empresa.com"), any());
    }

    @Test
    void downloadFile_ShouldReturnResource_WhenDocumentExists() throws Exception {
        byte[] fileBytes = "contenido simulado".getBytes();
        when(documentService.download(documentId)).thenReturn(fileBytes);
        when(documentService.findById(documentId)).thenReturn(documentDTO);

        mockMvc.perform(get("/api/v1/documents/{id}/download", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"contrato.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(documentService, times(1)).download(documentId);
        verify(documentService, times(1)).findById(documentId);
    }

    @Test
    void getMetadata_ShouldReturnDocumentDTO_WhenExists() throws Exception {
        when(documentService.findById(documentId)).thenReturn(documentDTO);

        mockMvc.perform(get("/api/v1/documents/{id}/metadata", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId.toString()))
                .andExpect(jsonPath("$.fileName").value("contrato.pdf"));

        verify(documentService, times(1)).findById(documentId);
    }

    @Test
    void previewDocument_ShouldReturnBytes_WhenExists() throws Exception {
        byte[] fileBytes = "contenido simulado".getBytes();
        when(documentService.findById(documentId)).thenReturn(documentDTO);
        when(documentService.download(documentId)).thenReturn(fileBytes);

        mockMvc.perform(get("/api/v1/documents/{id}/preview", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"contrato.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(documentService, times(1)).findById(documentId);
        verify(documentService, times(1)).download(documentId);
    }
}