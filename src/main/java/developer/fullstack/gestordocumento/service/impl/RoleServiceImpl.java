package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.RoleDTO;
import developer.fullstack.gestordocumento.dto.RoleRequestDTO;
import developer.fullstack.gestordocumento.entity.RoleEntity;
import developer.fullstack.gestordocumento.repository.RoleRepository;
import developer.fullstack.gestordocumento.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleDTO create(RoleRequestDTO dto) {
        if (roleRepository.findByName(dto.name()).isPresent()) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + dto.name());
        }

        RoleEntity role = new RoleEntity(dto.name(), dto.description());
        RoleEntity saved = roleRepository.save(role);

        return new RoleDTO(saved.getId(), saved.getName(), saved.getDescription());
    }

    @Override
    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleDTO(r.getId(), r.getName(), r.getDescription()))
                .toList();
    }

    @Override
    public RoleDTO findById(UUID id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
        return new RoleDTO(role.getId(), role.getName(), role.getDescription());
    }
}