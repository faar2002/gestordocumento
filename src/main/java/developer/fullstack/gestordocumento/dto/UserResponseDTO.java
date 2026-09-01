package developer.fullstack.gestordocumento.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String fullName,
    String email,
    CompanyDTO company,
    Set<String> authorizedSystemCodes
) {}