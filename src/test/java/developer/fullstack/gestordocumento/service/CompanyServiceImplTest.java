package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.entity.CompanyEntity;
import developer.fullstack.gestordocumento.repository.CompanyRepository;
import developer.fullstack.gestordocumento.service.impl.CompanyServiceImpl;
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
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private UUID companyId;
    private CompanyEntity companyEntity;
    private CompanyDTO companyDTO;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();

        companyEntity = new CompanyEntity();
        companyEntity.setId(companyId);
        companyEntity.setName("Transtecnia S.A.");
        companyEntity.setTaxId("76123456-7");

        companyDTO = new CompanyDTO(companyId, "Transtecnia S.A.", "76123456-7");
    }

    @Test
    void findById_ShouldReturnCompanyDTO_WhenExists() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyEntity));

        CompanyDTO result = companyService.findById(companyId);

        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Transtecnia S.A.", result.name());
        assertEquals("76123456-7", result.taxId());
        verify(companyRepository, times(1)).findById(companyId);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyService.findById(companyId);
        });

        assertTrue(exception.getMessage().contains("Empresa no encontrada"));
        verify(companyRepository, times(1)).findById(companyId);
    }

    @Test
    void findAll_ShouldReturnListOfCompanies() {
        when(companyRepository.findAll()).thenReturn(List.of(companyEntity));

        List<CompanyDTO> results = companyService.findAll();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Transtecnia S.A.", results.get(0).name());
        verify(companyRepository, times(1)).findAll();
    }

    @Test
    void create_ShouldSaveAndReturnCompanyDTO() {
        when(companyRepository.save(any(CompanyEntity.class))).thenReturn(companyEntity);

        CompanyDTO result = companyService.create(companyDTO);

        assertNotNull(result);
        assertEquals("Transtecnia S.A.", result.name());
        assertEquals("76123456-7", result.taxId());
        verify(companyRepository, times(1)).save(any(CompanyEntity.class));
    }

    @Test
    void update_ShouldUpdateAndReturnCompanyDTO_WhenExists() {
        CompanyDTO updateDTO = new CompanyDTO(null, "Transtecnia Actualizada", "76123456-8");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyEntity));
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyDTO result = companyService.update(companyId, updateDTO);

        assertNotNull(result);
        assertEquals("Transtecnia Actualizada", result.name());
        assertEquals("76123456-8", result.taxId());
        verify(companyRepository, times(1)).findById(companyId);
        verify(companyRepository, times(1)).save(any(CompanyEntity.class));
    }

    @Test
    void update_ShouldThrowException_WhenNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyService.update(companyId, companyDTO);
        });

        assertTrue(exception.getMessage().contains("Empresa no encontrada"));
        verify(companyRepository, times(1)).findById(companyId);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteCompany_WhenExists() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
        doNothing().when(companyRepository).deleteById(companyId);

        assertDoesNotThrow(() -> companyService.delete(companyId));
        verify(companyRepository, times(1)).existsById(companyId);
        verify(companyRepository, times(1)).deleteById(companyId);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        when(companyRepository.existsById(companyId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyService.delete(companyId);
        });

        assertTrue(exception.getMessage().contains("Empresa no encontrada"));
        verify(companyRepository, times(1)).existsById(companyId);
        verify(companyRepository, never()).deleteById(any());
    }
}
