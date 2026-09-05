package developer.fullstack.gestordocumento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class WorkGroupRequestDTO {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private UUID companyId;
}
