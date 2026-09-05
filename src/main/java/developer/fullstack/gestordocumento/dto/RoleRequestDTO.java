package developer.fullstack.gestordocumento.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequestDTO(
    @NotBlank(message = "El nombre del rol es obligatorio")
    String name,
    String description
) {}