package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.WorkGroupDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupRequestDTO;

import java.util.List;
import java.util.UUID;

public interface WorkGroupService {
    WorkGroupDTO create(WorkGroupRequestDTO dto);
    WorkGroupDTO findById(UUID id);
    List<WorkGroupDTO> findAll();
    List<WorkGroupDTO> findByCompanyId(UUID companyId);
    WorkGroupDTO update(UUID id, WorkGroupRequestDTO dto);
    void delete(UUID id);
}