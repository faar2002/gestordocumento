package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.dto.DocumentDTO;
import developer.fullstack.gestordocumento.dto.PageResponseDTO;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import developer.fullstack.gestordocumento.enums.DocumentStatus;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDTO> uploadFile(
            @RequestParam("file") MultipartFile file, 
            @RequestParam(value = "email", required = false) String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.upload(file, email));
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
                .contentType(MediaType.parseMediaType(metadata.fileType())) // Acceso directo al campo record
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.fileName() + "\"") // Acceso directo al campo record
                .body(new org.springframework.core.io.ByteArrayResource(data));
    }

    @GetMapping("/{id}/metadata")
    public ResponseEntity<DocumentDTO> getMetadata(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewDocument(@PathVariable UUID id) {
        // Buscamos el documento en la base de datos para obtener su tipo y nombre
        DocumentDTO document = documentService.findById(id); 
        byte[] fileBytes = documentService.download(id); // Reutilizamos tu método de lectura

        // Determinar el Content-Type (por defecto application/octet-stream si viene nulo)
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
                // Usamos "inline" para que el navegador intente abrirlo en lugar de descargarlo
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.fileName() + "\"")
                .body(fileBytes);
    }
}