package developer.fullstack.gestordocumento.dto;

import java.util.UUID;

public record CompanyDTO(
    UUID id,
    String name,
    String taxId
) {}