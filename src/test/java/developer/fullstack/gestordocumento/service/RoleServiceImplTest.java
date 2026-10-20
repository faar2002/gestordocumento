package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.RoleDTO;
import developer.fullstack.gestordocumento.dto.RoleRequestDTO;
import developer.fullstack.gestordocumento.entity.RoleEntity;
import developer.fullstack.gestordocumento.repository.RoleRepository;
import developer.fullstack.gestordocumento.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private UUID roleId;
    private RoleEntity roleEntity;
    private RoleRequestDTO roleRequestDTO;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        
        // Inicializamos la entidad de prueba basada en RoleServiceImpl[cite: 7]
        roleEntity = new RoleEntity("ADMIN", "Administrador del sistema");
    }

    @Test
    void findById_ShouldReturnRoleDTO_WhenRoleExists() {
        // Arrange[cite: 7]
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(roleEntity));

        // Act[cite: 7]
        RoleDTO result = roleService.findById(roleId);

        // Assert[cite: 7]
        assertNotNull(result);
        assertEquals("ADMIN", result.name());
        assertEquals("Administrador del sistema", result.description());
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void findById_ShouldThrowException_WhenRoleNotFound() {
        // Arrange[cite: 7]
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert[cite: 7]
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.findById(roleId);
        });

        assertTrue(exception.getMessage().contains("Rol no encontrado"));
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void findAll_ShouldReturnListOfRoles() {
        // Arrange[cite: 7]
        when(roleRepository.findAll()).thenReturn(List.of(roleEntity));

        // Act[cite: 7]
        List<RoleDTO> results = roleService.findAll();

        // Assert[cite: 7]
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("ADMIN", results.get(0).name());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void create_ShouldSaveAndReturnRoleDTO_WhenNameIsUnique() {
        // Arrange[cite: 7]
        roleRequestDTO = new RoleRequestDTO("ADMIN", "Administrador del sistema");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        // Act[cite: 7]
        RoleDTO result = roleService.create(roleRequestDTO);

        // Assert[cite: 7]
        assertNotNull(result);
        assertEquals("ADMIN", result.name());
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, times(1)).save(any(RoleEntity.class));
    }

    @Test
    void create_ShouldThrowException_WhenRoleNameAlreadyExists() {
        // Arrange[cite: 7]
        roleRequestDTO = new RoleRequestDTO("ADMIN", "Administrador del sistema");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(roleEntity));

        // Act & Assert[cite: 7]
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.create(roleRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe un rol con el nombre"));
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, never()).save(any(RoleEntity.class));
    }
}