package developer.fullstack.gestordocumento.dto;

import java.util.UUID;

public record FirmanteResponseDTO(
    UUID uuid,
    String nombreFirmante,
    String emailFirmante,
    Integer orden,
    String estado
) {}