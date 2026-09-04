package developer.fullstack.gestordocumento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponseDTO(
    UUID id,
    String originalName,
    String contentType,
    long size,
    LocalDateTime uploadedAt,
    String email
) {}