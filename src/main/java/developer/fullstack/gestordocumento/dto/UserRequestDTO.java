package developer.fullstack.gestordocumento.dto;

import java.util.Set;
import java.util.UUID;

public record UserRequestDTO(
    String fullName,
    String email,
    UUID companyId,
    Set<UUID> systemIds
) {}
