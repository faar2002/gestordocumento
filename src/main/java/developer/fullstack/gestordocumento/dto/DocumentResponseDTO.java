package developer.fullstack.gestordocumento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import developer.fullstack.gestordocumento.enums.DocumentStatus;

public record DocumentResponseDTO(
    UUID id,
    String originalName,
    String contentType,
    long size,
    LocalDateTime uploadedAt,
    String email,
    DocumentStatus status
) {}