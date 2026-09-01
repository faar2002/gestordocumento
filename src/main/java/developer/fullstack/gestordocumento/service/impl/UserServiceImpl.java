package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.dto.UserRequestDTO;
import developer.fullstack.gestordocumento.dto.UserResponseDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.entity.SystemAccessEntity;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.repository.CompanyRepository;
import developer.fullstack.gestordocumento.repository.SystemAccessRepository;
import developer.fullstack.gestordocumento.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           CompanyRepository companyRepository,
                           SystemAccessRepository systemAccessRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.systemAccessRepository = systemAccessRepository;
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
        CompanyDTO companyDTO = user.getCompany() != null
                ? new CompanyDTO(user.getCompany().getId(), user.getCompany().getName(), user.getCompany().getTaxId())
                : null;

        Set<String> systemCodes = user.getAuthorizedSystems() != null
                ? user.getAuthorizedSystems().stream().map(SystemAccessEntity::getSystemCode).collect(Collectors.toSet())
                : Set.of();

        // Concatenación amigable para mostrar el nombre completo en tablas de Angular
        String fullName = String.format("%s %s %s %s",
                user.getFirstName(),
                user.getMiddleName() != null ? user.getMiddleName() : "",
                user.getLastName(),
                user.getSecondLastName() != null ? user.getSecondLastName() : ""
        ).replaceAll("\\s+", " ").trim();

        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getSecondLastName(),
                fullName,
                user.getEmail(),
                companyDTO,
                systemCodes
        );
    }
}