package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.AuthResponseDTO;
import developer.fullstack.gestordocumento.dto.LoginRequestDTO;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.exception.AuthException;
import developer.fullstack.gestordocumento.repository.UserRepository;
import developer.fullstack.gestordocumento.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity userEntity;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setFirstName("Francisco");
        userEntity.setLastName("Aponte");
        userEntity.setEmail("faponte@yopmail.com");
        userEntity.setPassword("$2a$10$encodedPasswordHash");
        userEntity.setEnabled(true);
        userEntity.setFailedAttempts(0);
        userEntity.setLockTime(null);

        loginRequest = new LoginRequestDTO("faponte@yopmail.com", "Password123!");
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.password(), userEntity.getPassword())).thenReturn(true);
        // REMOVIDO: when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity); -> No es necesario porque failedAttempts es 0 y lockTime es null

        // Act
        AuthResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Inicio de sesión exitoso", response.message());
        assertNotNull(response.user());
        assertEquals("faponte@yopmail.com", response.user().email());

        verify(userRepository, times(1)).findByEmail(loginRequest.email());
        verify(passwordEncoder, times(1)).matches(loginRequest.password(), userEntity.getPassword());
    }

    @Test
    void login_ShouldThrowException_WhenEmailDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("El correo electrónico no existe", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(userRepository, times(1)).findByEmail(loginRequest.email());
    }

    @Test
    void login_ShouldThrowException_WhenUserIsDisabled() {
        // Arrange
        userEntity.setEnabled(false);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(userEntity));

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("El usuario se encuentra deshabilitado.", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void login_ShouldThrowException_WhenAccountIsLocked() {
        // Arrange
        userEntity.setLockTime(LocalDateTime.now()); // Bloqueada actualmente
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(userEntity));

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("La cuenta está bloqueada"));
        assertEquals(HttpStatus.LOCKED, exception.getStatus());
    }

    @Test
    void login_ShouldProcessFailedAttempt_WhenPasswordIsInvalid() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.password(), userEntity.getPassword())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Contraseña inválida"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void login_ShouldLockAccount_WhenMaxFailedAttemptsReached() {
        // Arrange
        userEntity.setFailedAttempts(2); // Al fallar este intento llegará a 3 (MAX_FAILED_ATTEMPTS)
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.password(), userEntity.getPassword())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Ha superado el límite de intentos"));
        assertEquals(HttpStatus.LOCKED, exception.getStatus());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}