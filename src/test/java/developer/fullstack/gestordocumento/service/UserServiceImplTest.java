package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.UserRequestDTO;
import developer.fullstack.gestordocumento.dto.UserResponseDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.entity.UserEntity;
import developer.fullstack.gestordocumento.repository.*;
import developer.fullstack.gestordocumento.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SystemAccessRepository systemAccessRepository;

    @Mock
    private WorkGroupRepository workGroupRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private UUID companyId;
    private UserEntity userEntity;
    private UserRequestDTO userRequestDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Empresa Test S.A.");
        company.setTaxId("76123456-7");

        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Francisco");
        userEntity.setMiddleName("Antonio");
        userEntity.setLastName("Aponte");
        userEntity.setSecondLastName("Rivas");
        userEntity.setEmail("faponte@yopmail.com");
        userEntity.setPassword("encodedPassword");
        userEntity.setEnabled(true);
        userEntity.setCompany(company);

        userRequestDTO = new UserRequestDTO(
                "Francisco",
                "Antonio",
                "Aponte",
                "Rivas",
                "faponte@yopmail.com",
                "Password123!",
                companyId,
                Set.of(),
                Set.of(),
                Set.of()
        );
    }

    @Test
    void findById_ShouldReturnUserResponseDTO_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        UserResponseDTO result = userService.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("faponte@yopmail.com", result.email());
        assertEquals("Francisco Antonio Aponte Rivas", result.fullName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findById(userId);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findAll_ShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));

        List<UserResponseDTO> results = userService.findAll();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("faponte@yopmail.com", results.get(0).email());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void create_ShouldSaveAndReturnUser_WhenDataIsValid() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(userRequestDTO.password())).thenReturn("encodedPassword");
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(userEntity.getCompany()));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserResponseDTO result = userService.create(userRequestDTO);

        assertNotNull(result);
        assertEquals("faponte@yopmail.com", result.email());
        verify(userRepository, times(1)).existsByEmail(userRequestDTO.email());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.create(userRequestDTO);
        });

        assertTrue(exception.getMessage().contains("El correo ya está registrado"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void delete_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        assertDoesNotThrow(() -> userService.delete(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void toggleUserStatus_ShouldUpdateStatus_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserResponseDTO result = userService.toggleUserStatus(userId, false);

        assertNotNull(result);
        assertFalse(result.enabled());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}