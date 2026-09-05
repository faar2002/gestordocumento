package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupRequestDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.entity.WorkGroupEntity;
import developer.fullstack.gestordocumento.repository.CompanyRepository;
import developer.fullstack.gestordocumento.repository.WorkGroupRepository;
import developer.fullstack.gestordocumento.service.WorkGroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkGroupServiceImpl implements WorkGroupService {

    private final WorkGroupRepository workGroupRepository;
    private final CompanyRepository companyRepository;

    public WorkGroupServiceImpl(WorkGroupRepository workGroupRepository, CompanyRepository companyRepository) {
        this.workGroupRepository = workGroupRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public WorkGroupDTO create(WorkGroupRequestDTO dto) {
        if (dto.getCompanyId() != null && workGroupRepository.existsByNameAndCompanyId(dto.getName(), dto.getCompanyId())) {
            throw new RuntimeException("Ya existe un grupo de trabajo con el nombre '" + dto.getName() + "' para esta empresa.");
        }

        WorkGroupEntity entity = new WorkGroupEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        if (dto.getCompanyId() != null) {
            CompanyEntity company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.getCompanyId()));
            entity.setCompany(company);
        }

        WorkGroupEntity saved = workGroupRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public WorkGroupDTO update(UUID id, WorkGroupRequestDTO dto) {
        WorkGroupEntity entity = workGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo de trabajo no encontrado con id: " + id));

        if (dto.getCompanyId() != null && workGroupRepository.existsByNameAndCompanyIdAndIdNot(dto.getName(), dto.getCompanyId(), id)) {
            throw new RuntimeException("Ya existe un grupo de trabajo con el nombre '" + dto.getName() + "' para esta empresa.");
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        if (dto.getCompanyId() != null) {
            CompanyEntity company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.getCompanyId()));
            entity.setCompany(company);
        } else {
            entity.setCompany(null);
        }

        return mapToDTO(workGroupRepository.save(entity));
    }

    @Override
    public WorkGroupDTO findById(UUID id) {
        WorkGroupEntity entity = workGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo de trabajo no encontrado con id: " + id));
        return mapToDTO(entity);
    }

    @Override
    public List<WorkGroupDTO> findAll() {
        return workGroupRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<WorkGroupDTO> findByCompanyId(UUID companyId) {
        return workGroupRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        if (!workGroupRepository.existsById(id)) {
            throw new RuntimeException("Grupo de trabajo no encontrado con id: " + id);
        }
        workGroupRepository.deleteById(id);
    }

    private WorkGroupDTO mapToDTO(WorkGroupEntity entity) {
        CompanyDTO companyDTO = entity.getCompany() != null
                ? new CompanyDTO(entity.getCompany().getId(), entity.getCompany().getName(), entity.getCompany().getTaxId())
                : null;

        return new WorkGroupDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                companyDTO
        );
    }
}