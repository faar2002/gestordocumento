package developer.fullstack.gestordocumento.dto;

import java.util.UUID;

public record RoleDTO(
    UUID id,
    String name,
    String description
) {}