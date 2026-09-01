package developer.fullstack.gestordocumento.dto;

import java.util.UUID;

public record WorkGroupRequestDTO(
    String name,
    String description,
    UUID companyId
) {}
