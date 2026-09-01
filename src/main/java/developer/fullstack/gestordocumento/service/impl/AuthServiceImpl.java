package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.dto.AuthResponseDTO;
import developer.fullstack.gestordocumento.dto.LoginRequestDTO;
import developer.fullstack.gestordocumento.dto.UserResponseDTO;
import developer.fullstack.gestordocumento.entity.SystemAccessEntity;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        // 1. Buscar usuario por email
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Validar la contraseña enviada contra el Hash BCrypt almacenado
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // 3. Mapear la información del usuario para retornar al Frontend
        UserResponseDTO userDTO = mapToUserDTO(user);

        return new AuthResponseDTO("Inicio de sesión exitoso", userDTO);
    }

    private UserResponseDTO mapToUserDTO(UserEntity user) {
        CompanyDTO companyDTO = user.getCompany() != null
                ? new CompanyDTO(user.getCompany().getId(), user.getCompany().getName(), user.getCompany().getTaxId())
                : null;

        Set<String> systemCodes = user.getAuthorizedSystems() != null
                ? user.getAuthorizedSystems().stream().map(SystemAccessEntity::getSystemCode).collect(Collectors.toSet())
                : Set.of();

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