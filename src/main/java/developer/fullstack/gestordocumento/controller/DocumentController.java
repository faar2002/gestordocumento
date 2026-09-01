package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.dto.DocumentResponseDTO;
import developer.fullstack.gestordocumento.entity.DocumentEntity;
import developer.fullstack.gestordocumento.service.StorageService;
import org.springframework.core.io.Resource;
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

    private final StorageService storageService;

    public DocumentController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "email", required = false) String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.upload(file,email));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> listAllFiles() {
        return ResponseEntity.ok(storageService.listAll());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        Resource resource = storageService.download(id);
        DocumentEntity metadata = storageService.getMetadata(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/metadata")
    public ResponseEntity<DocumentEntity> getMetadata(@PathVariable UUID id) {
        return ResponseEntity.ok(storageService.getMetadata(id));
    }
}