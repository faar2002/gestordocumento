package developer.fullstack.gestordocumento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentDTO(
    UUID id,
    String fileName,
    String fileType,
    long fileSize,
    LocalDateTime uploadDate,
    String userEmail
) {}
