package developer.fullstack.gestordocumento.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String firstName,
    String middleName,
    String lastName,
    String secondLastName,
    String fullName, // Campo calculado útil para la UI
    String email,
    CompanyDTO company,
    Set<String> authorizedSystemCodes
) {}