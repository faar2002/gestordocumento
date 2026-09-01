package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.*;
import developer.fullstack.gestordocumento.entity.*;
import developer.fullstack.gestordocumento.repository.*;
import developer.fullstack.gestordocumento.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final SystemAccessRepository systemAccessRepository;
    private final WorkGroupRepository workGroupRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           CompanyRepository companyRepository,
                           SystemAccessRepository systemAccessRepository,
                           WorkGroupRepository workGroupRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.systemAccessRepository = systemAccessRepository;
        this.workGroupRepository = workGroupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        UserEntity user = new UserEntity();
        user.setFirstName(dto.firstName());
        user.setMiddleName(dto.middleName());
        user.setLastName(dto.lastName());
        user.setSecondLastName(dto.secondLastName());
        user.setEmail(dto.email());
        
        // Encriptar la contraseña recibida antes de guardar en PostgreSQL
        user.setPassword(passwordEncoder.encode(dto.password()));

        if (dto.companyId() != null) {
            CompanyEntity company = companyRepository.findById(dto.companyId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.companyId()));
            user.setCompany(company);
        }

        if (dto.systemIds() != null && !dto.systemIds().isEmpty()) {
            List<SystemAccessEntity> systems = systemAccessRepository.findAllById(dto.systemIds());
            user.setAuthorizedSystems(new HashSet<>(systems));
        }

        // Cargar y asignar Grupos de Trabajo desde sus UUIDs
        if (dto.workGroupIds() != null && !dto.workGroupIds().isEmpty()) {
            List<WorkGroupEntity> workGroups = workGroupRepository.findAllById(dto.workGroupIds());
            user.setWorkGroups(new HashSet<>(workGroups));
        } else {
            user.setWorkGroups(new HashSet<>());
        }

        return mapToDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        return mapToDTO(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        user.setFirstName(dto.firstName());
        user.setMiddleName(dto.middleName());
        user.setLastName(dto.lastName());
        user.setSecondLastName(dto.secondLastName());
        user.setEmail(dto.email());
        
        // Re-encriptar solo si se envió una nueva clave
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        if (dto.companyId() != null) {
            CompanyEntity company = companyRepository.findById(dto.companyId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.companyId()));
            user.setCompany(company);
        } else {
            user.setCompany(null);
        }

        if (dto.systemIds() != null) {
            List<SystemAccessEntity> systems = systemAccessRepository.findAllById(dto.systemIds());
            user.setAuthorizedSystems(new HashSet<>(systems));
        }

        return mapToDTO(userRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO mapToDTO(UserEntity user) {
        // 1. Mapeo de Empresa
        CompanyDTO companyDTO = user.getCompany() != null
                ? new CompanyDTO(
                    user.getCompany().getId(), 
                    user.getCompany().getName(), 
                    user.getCompany().getTaxId()
                )
                : null;

        // 2. Mapeo de Códigos de Sistemas Autorizados
        Set<String> systemCodes = user.getAuthorizedSystems() != null
                ? user.getAuthorizedSystems().stream()
                        .map(SystemAccessEntity::getSystemCode)
                        .collect(Collectors.toSet())
                : Set.of();

        // 3. Mapeo de Grupos de Trabajo (WorkGroupDTO con CompanyDTO embebido)
        Set<WorkGroupDTO> workGroupDTOs = user.getWorkGroups() != null
            ? user.getWorkGroups().stream()
                    .map(this::mapWorkGroupToDTO)
                    .collect(Collectors.toSet())
            : Set.of();

        // 4. Concatenación amigable del nombre completo
        String fullName = String.format("%s %s %s %s",
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getMiddleName() != null ? user.getMiddleName() : "",
                user.getLastName() != null ? user.getLastName() : "",
                user.getSecondLastName() != null ? user.getSecondLastName() : ""
        ).replaceAll("\\s+", " ").trim();

        // 5. Retorno del DTO de respuesta
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getSecondLastName(),
                fullName,
                user.getEmail(),
                user.getEnabled(),
                companyDTO,
                systemCodes,
                workGroupDTOs
        );
    }

    private WorkGroupDTO mapWorkGroupToDTO(WorkGroupEntity group) {
        CompanyDTO companyDTO = group.getCompany() != null
                ? new CompanyDTO(group.getCompany().getId(), group.getCompany().getName(), group.getCompany().getTaxId())
                : null;

        return new WorkGroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                companyDTO
        );
    }

    @Override
    public UserResponseDTO toggleUserStatus(UUID id, boolean enabled) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setEnabled(enabled);
        return mapToDTO(userRepository.save(user));
    }
}