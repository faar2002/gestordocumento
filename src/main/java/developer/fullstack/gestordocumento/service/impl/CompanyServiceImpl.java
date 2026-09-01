package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.repository.CompanyRepository;
import developer.fullstack.gestordocumento.service.CompanyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyDTO create(CompanyDTO dto) {
        CompanyEntity entity = new CompanyEntity();
        entity.setName(dto.name());
        entity.setTaxId(dto.taxId());

        CompanyEntity saved = companyRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public CompanyDTO findById(UUID id) {
        CompanyEntity entity = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
        return mapToDTO(entity);
    }

    @Override
    public List<CompanyDTO> findAll() {
        return companyRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public CompanyDTO update(UUID id, CompanyDTO dto) {
        CompanyEntity entity = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
        
        entity.setName(dto.name());
        entity.setTaxId(dto.taxId());

        return mapToDTO(companyRepository.save(entity));
    }

    @Override
    public void delete(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Empresa no encontrada con id: " + id);
        }
        companyRepository.deleteById(id);
    }

    private CompanyDTO mapToDTO(CompanyEntity entity) {
        return new CompanyDTO(entity.getId(), entity.getName(), entity.getTaxId());
    }
}