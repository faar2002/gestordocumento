package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.WorkGroupDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupRequestDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.entity.WorkGroupEntity;
import developer.fullstack.gestordocumento.repository.CompanyRepository;
import developer.fullstack.gestordocumento.repository.WorkGroupRepository;
import developer.fullstack.gestordocumento.service.impl.WorkGroupServiceImpl;
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
class WorkGroupServiceImplTest {

    @Mock
    private WorkGroupRepository workGroupRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private WorkGroupServiceImpl workGroupService;

    private UUID workGroupId;
    private UUID companyId;
    private WorkGroupEntity workGroupEntity;
    private CompanyEntity companyEntity;
    private WorkGroupRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        workGroupId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        companyEntity = new CompanyEntity();
        companyEntity.setId(companyId);
        companyEntity.setName("Empresa Test S.A.");
        companyEntity.setTaxId("76123456-7");

        workGroupEntity = new WorkGroupEntity();
        workGroupEntity.setId(workGroupId);
        workGroupEntity.setName("Desarrollo Backend");
        workGroupEntity.setDescription("Equipo de microservicios Spring Boot");
        workGroupEntity.setCompany(companyEntity);

        requestDTO = new WorkGroupRequestDTO();
        // Asumiendo métodos setter o constructor en WorkGroupRequestDTO según tu implementación
        // Si es un record o usa setters, asegúrate de ajustarlo. Aquí simularemos llamadas comunes:
        // Nota: Si WorkGroupRequestDTO usa getters (dto.getName()), inicializamos acorde.
    }

    @Test
    void findById_ShouldReturnWorkGroupDTO_WhenExists() {
        when(workGroupRepository.findById(workGroupId)).thenReturn(Optional.of(workGroupEntity));

        WorkGroupDTO result = workGroupService.findById(workGroupId);

        assertNotNull(result);
        assertEquals(workGroupId, result.id());
        assertEquals("Desarrollo Backend", result.name());
        assertNotNull(result.company());
        assertEquals(companyId, result.company().id());
        verify(workGroupRepository, times(1)).findById(workGroupId);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(workGroupRepository.findById(workGroupId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workGroupService.findById(workGroupId);
        });

        assertTrue(exception.getMessage().contains("Grupo de trabajo no encontrado"));
        verify(workGroupRepository, times(1)).findById(workGroupId);
    }

    @Test
    void findAll_ShouldReturnListOfWorkGroups() {
        when(workGroupRepository.findAll()).thenReturn(List.of(workGroupEntity));

        List<WorkGroupDTO> results = workGroupService.findAll();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Desarrollo Backend", results.get(0).name());
        verify(workGroupRepository, times(1)).findAll();
    }

    @Test
    void findByCompanyId_ShouldReturnListFilteredByCompany() {
        when(workGroupRepository.findByCompanyId(companyId)).thenReturn(List.of(workGroupEntity));

        List<WorkGroupDTO> results = workGroupService.findByCompanyId(companyId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Desarrollo Backend", results.get(0).name());
        verify(workGroupRepository, times(1)).findByCompanyId(companyId);
    }

    @Test
    void create_ShouldSaveAndReturnDTO_WhenValid() {
        WorkGroupRequestDTO dto = mock(WorkGroupRequestDTO.class);
        when(dto.getName()).thenReturn("Infraestructura");
        when(dto.getDescription()).thenReturn("Equipo Cloud");
        when(dto.getCompanyId()).thenReturn(companyId);

        when(workGroupRepository.existsByNameAndCompanyId("Infraestructura", companyId)).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyEntity));
        when(workGroupRepository.save(any(WorkGroupEntity.class))).thenAnswer(invocation -> {
            WorkGroupEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        WorkGroupDTO result = workGroupService.create(dto);

        assertNotNull(result);
        assertEquals("Infraestructura", result.name());
        verify(workGroupRepository, times(1)).existsByNameAndCompanyId("Infraestructura", companyId);
        verify(workGroupRepository, times(1)).save(any(WorkGroupEntity.class));
    }

    @Test
    void create_ShouldThrowException_WhenNameExistsForSameCompany() {
        WorkGroupRequestDTO dto = mock(WorkGroupRequestDTO.class);
        when(dto.getName()).thenReturn("Desarrollo Backend");
        when(dto.getCompanyId()).thenReturn(companyId);

        when(workGroupRepository.existsByNameAndCompanyId("Desarrollo Backend", companyId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workGroupService.create(dto);
        });

        assertTrue(exception.getMessage().contains("Ya existe un grupo de trabajo"));
        verify(workGroupRepository, never()).save(any(WorkGroupEntity.class));
    }

    @Test
    void delete_ShouldDeleteWorkGroup_WhenExists() {
        when(workGroupRepository.existsById(workGroupId)).thenReturn(true);
        doNothing().when(workGroupRepository).deleteById(workGroupId);

        assertDoesNotThrow(() -> workGroupService.delete(workGroupId));
        verify(workGroupRepository, times(1)).deleteById(workGroupId);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        when(workGroupRepository.existsById(workGroupId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workGroupService.delete(workGroupId);
        });

        assertTrue(exception.getMessage().contains("Grupo de trabajo no encontrado"));
        verify(workGroupRepository, never()).deleteById(any());
    }
}