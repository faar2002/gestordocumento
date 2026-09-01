package developer.fullstack.gestordocumento.dto;

import java.util.Set;
import java.util.UUID;


public record UserRequestDTO(
    String firstName,
    String middleName,
    String lastName,
    String secondLastName,
    String email,
    String password,
    UUID companyId,
    Set<UUID> systemIds,
    Set<UUID> workGroupIds // IDs de los grupos asignados
) {}
