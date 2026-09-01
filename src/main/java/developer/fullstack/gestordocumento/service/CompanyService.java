package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.CompanyDTO;

import java.util.List;
import java.util.UUID;

public interface CompanyService {
    CompanyDTO create(CompanyDTO dto);
    CompanyDTO findById(UUID id);
    List<CompanyDTO> findAll();
    CompanyDTO update(UUID id, CompanyDTO dto);
    void delete(UUID id);
}