package developer.fullstack.gestordocumento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    private UUID companyId;
    private CompanyDTO companyDTO;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        companyDTO = new CompanyDTO(
                companyId,
                "Transtecnia S.A.",
                "76123456-7"
        );
    }

    @Test
    void create_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        when(companyService.create(any(CompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").value("Transtecnia S.A."))
                .andExpect(jsonPath("$.taxId").value("76123456-7"));

        verify(companyService, times(1)).create(any(CompanyDTO.class));
    }

    @Test
    void findAll_ShouldReturnList_WhenCompaniesExist() throws Exception {
        when(companyService.findAll()).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Transtecnia S.A."))
                .andExpect(jsonPath("$[0].taxId").value("76123456-7"));

        verify(companyService, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnCompany_WhenExists() throws Exception {
        when(companyService.findById(companyId)).thenReturn(companyDTO);

        mockMvc.perform(get("/api/v1/companies/{id}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").value("Transtecnia S.A."));

        verify(companyService, times(1)).findById(companyId);
    }

    @Test
    void update_ShouldReturnUpdated_WhenExists() throws Exception {
        CompanyDTO updatedDTO = new CompanyDTO(companyId, "Transtecnia Actualizada", "76123456-8");
        when(companyService.update(eq(companyId), any(CompanyDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/v1/companies/{id}", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Transtecnia Actualizada"))
                .andExpect(jsonPath("$.taxId").value("76123456-8"));

        verify(companyService, times(1)).update(eq(companyId), any(CompanyDTO.class));
    }

    @Test
    void delete_ShouldReturnNoContent_WhenExists() throws Exception {
        doNothing().when(companyService).delete(companyId);

        mockMvc.perform(delete("/api/v1/companies/{id}", companyId))
                .andExpect(status().isNoContent());

        verify(companyService, times(1)).delete(companyId);
    }
}