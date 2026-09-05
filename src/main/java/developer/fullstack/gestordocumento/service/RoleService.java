package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.RoleDTO;
import developer.fullstack.gestordocumento.dto.RoleRequestDTO;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDTO create(RoleRequestDTO dto);
    List<RoleDTO> findAll();
    RoleDTO findById(UUID id);
}