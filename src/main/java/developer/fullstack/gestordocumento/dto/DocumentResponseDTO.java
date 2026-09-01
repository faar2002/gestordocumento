package developer.fullstack.gestordocumento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponseDTO(
    UUID id,
    String fileName,
    String contentType,
    Long size,
    String email,
    LocalDateTime uploadedAt
) {}
