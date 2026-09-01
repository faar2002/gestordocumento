package developer.fullstack.gestordocumento.dto;

public record AuthResponseDTO(
    String message,
    UserResponseDTO user
) {}