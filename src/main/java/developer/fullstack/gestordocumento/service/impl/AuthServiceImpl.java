package developer.fullstack.gestordocumento.service.impl;

import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.dto.AuthResponseDTO;
import developer.fullstack.gestordocumento.dto.LoginRequestDTO;
import developer.fullstack.gestordocumento.dto.UserResponseDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupDTO;
import developer.fullstack.gestordocumento.entity.SystemAccessEntity;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.exception.AuthException;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_TIME_MINUTES = 5;

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
                .orElseThrow(() -> new AuthException("El correo electrónico no existe", HttpStatus.NOT_FOUND));

        // 2. Validar si la cuenta está habilitada (manejo seguro de nulos)
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AuthException("El usuario se encuentra deshabilitado.", HttpStatus.FORBIDDEN);
        }

        // 3. Validar si la cuenta se encuentra bloqueada
        if (isAccountLocked(user)) {
            long minutesRemaining = getMinutesRemaining(user.getLockTime());
            throw new AuthException(
                    String.format("La cuenta está bloqueada por demasiados intentos fallidos. Intente nuevamente en %d minuto(s).", minutesRemaining),
                    HttpStatus.LOCKED
            );
        }

        // 4. Validar la contraseña enviada contra el Hash BCrypt almacenado
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            processFailedAttempt(user);
            int currentAttempts = user.getFailedAttempts() != null ? user.getFailedAttempts() : 0;
            
            if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
                throw new AuthException("Contraseña inválida. Ha superado el límite de intentos. Cuenta bloqueada por 5 minutos.", HttpStatus.LOCKED);
            } else {
                throw new AuthException(String.format("Contraseña inválida. Intento %d de %d.", currentAttempts, MAX_FAILED_ATTEMPTS), HttpStatus.UNAUTHORIZED);
            }
        }

        // 5. Credenciales correctas: Reiniciar intentos fallidos y quitar bloqueo si existía
        resetFailedAttempts(user);

        // 6. Mapear la información del usuario para retornar al Frontend (Llamada corregida a mapToDTO)
        UserResponseDTO userDTO = mapToDTO(user);

        return new AuthResponseDTO("Inicio de sesión exitoso", userDTO);
    }
    
    private boolean isAccountLocked(UserEntity user) {
        if (user.getLockTime() == null) {
            return false;
        }
        // Si ya pasaron los 5 minutos, desbloquea automáticamente la cuenta
        if (user.getLockTime().plusMinutes(LOCK_TIME_MINUTES).isBefore(LocalDateTime.now())) {
            user.setLockTime(null);
            user.setFailedAttempts(0);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    private void processFailedAttempt(UserEntity user) {
        int currentAttempts = user.getFailedAttempts() != null ? user.getFailedAttempts() : 0;
        int newAttempts = currentAttempts + 1;
        user.setFailedAttempts(newAttempts);

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockTime(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(UserEntity user) {
        if ((user.getFailedAttempts() != null && user.getFailedAttempts() > 0) || user.getLockTime() != null) {
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        }
    }

    private long getMinutesRemaining(LocalDateTime lockTime) {
        LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_TIME_MINUTES);
        long secondsRemaining = Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
        return Math.max(1, (secondsRemaining + 59) / 60);
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
                        .map(group -> {
                            CompanyDTO groupCompanyDTO = group.getCompany() != null
                                    ? new CompanyDTO(
                                        group.getCompany().getId(),
                                        group.getCompany().getName(),
                                        group.getCompany().getTaxId()
                                    )
                                    : null;

                            return new WorkGroupDTO(
                                    group.getId(),
                                    group.getName(),
                                    group.getDescription(),
                                    groupCompanyDTO
                            );
                        })
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
}