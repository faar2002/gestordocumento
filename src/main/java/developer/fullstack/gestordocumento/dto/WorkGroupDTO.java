package developer.fullstack.gestordocumento.dto;

import java.util.UUID;

public record WorkGroupDTO(
    UUID id,
    String name,
    String description,
    CompanyDTO company
) {}
