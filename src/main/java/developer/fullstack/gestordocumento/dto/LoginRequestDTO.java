package developer.fullstack.gestordocumento.dto;

public record LoginRequestDTO(
    String email,
    String password
) {}