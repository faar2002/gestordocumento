package developer.fullstack.gestordocumento.dto;

public record FirmanteUploadDTO(
    String nombre,
    String email,      // Correo del firmante (puede o no existir en la base de datos)
    Integer orden      // Opcional, si manejas un orden secuencial de firma
) {}