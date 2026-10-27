package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.DocumentUploadRequestDTO; 
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Endpoint actualizado para soportar la subida del documento junto con 
     * una lista de firmantes (correos y nombres), permitiendo correos externos/nuevos.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDTO> uploadFile(
            @RequestParam("file") MultipartFile file, 
            @RequestParam(value = "email", required = false) String email,
            @RequestPart(value = "data", required = false) DocumentUploadRequestDTO request) {
        
        // Extraemos la lista de firmantes del DTO si viene presente en la petición
        var firmantes = (request != null) ? request.firmantes() : null;

        // Llamamos al servicio (si mantuviste el método con sobrecarga o uploadWithSigners)
        // Nota: Asegúrate de que el nombre del método en DocumentService coincida exactamente con este:
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadWithSigners(file, email, firmantes));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<DocumentDTO>> listAllFilesPaginated(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return ResponseEntity.ok(documentService.findAllPaginated(companyId, null, pageable));
    }

    /**
     * Búsqueda paginada flexible: admite filtrado opcional por empresa, correo o ambos.
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<DocumentDTO>> searchDocuments(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(documentService.findAllPaginated(companyId, email, pageable));
    }

   @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        byte[] data = documentService.download(id);
        DocumentDTO metadata = documentService.findById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.fileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.fileName() + "\"")
                .body(new org.springframework.core.io.ByteArrayResource(data));
    }

    @GetMapping("/{id}/metadata")
    public ResponseEntity<DocumentDTO> getMetadata(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewDocument(@PathVariable UUID id) {
        DocumentDTO document = documentService.findById(id); 
        byte[] fileBytes = documentService.download(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (document.fileType() != null && !document.fileType().isBlank()) {
                mediaType = MediaType.parseMediaType(document.fileType());
            }
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.fileName() + "\"")
                .body(fileBytes);
    }
}