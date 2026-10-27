package developer.fullstack.gestordocumento.dto;

import java.util.List;

public record DocumentUploadRequestDTO(
    List<FirmanteUploadDTO> firmantes
) {}