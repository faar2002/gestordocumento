package developer.fullstack.gestordocumento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import developer.fullstack.gestordocumento.dto.CompanyDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupRequestDTO;
import developer.fullstack.gestordocumento.service.WorkGroupService;
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

@WebMvcTest(WorkGroupController.class)
class WorkGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkGroupService workGroupService;

    private UUID workGroupId;
    private UUID companyId;
    private WorkGroupDTO workGroupDTO;
    private WorkGroupRequestDTO workGroupRequestDTO;

    @BeforeEach
    void setUp() {
        workGroupId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        CompanyDTO companyDTO = new CompanyDTO(companyId, "Transtecnia S.A.", "76123456-7");

        workGroupDTO = new WorkGroupDTO(
                workGroupId,
                "Desarrollo Backend",
                "Equipo de microservicios Spring Boot",
                companyDTO
        );

        workGroupRequestDTO = new WorkGroupRequestDTO();
        workGroupRequestDTO.setName("Desarrollo Backend");
        workGroupRequestDTO.setDescription("Equipo de microservicios Spring Boot");
        workGroupRequestDTO.setCompanyId(companyId);
    }

    @Test
    void createWorkGroup_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        when(workGroupService.create(any(WorkGroupRequestDTO.class))).thenReturn(workGroupDTO);

        mockMvc.perform(post("/api/v1/work-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workGroupRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workGroupId.toString()))
                .andExpect(jsonPath("$.name").value("Desarrollo Backend"))
                .andExpect(jsonPath("$.company.id").value(companyId.toString()));

        verify(workGroupService, times(1)).create(any(WorkGroupRequestDTO.class));
    }

    @Test
    void getWorkGroupById_ShouldReturnWorkGroup_WhenExists() throws Exception {
        when(workGroupService.findById(workGroupId)).thenReturn(workGroupDTO);

        mockMvc.perform(get("/api/v1/work-groups/{id}", workGroupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workGroupId.toString()))
                .andExpect(jsonPath("$.name").value("Desarrollo Backend"));

        verify(workGroupService, times(1)).findById(workGroupId);
    }

    @Test
    void getAllWorkGroups_ShouldReturnList() throws Exception {
        when(workGroupService.findAll()).thenReturn(List.of(workGroupDTO));

        mockMvc.perform(get("/api/v1/work-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Desarrollo Backend"));

        verify(workGroupService, times(1)).findAll();
    }

    @Test
    void getWorkGroupsByCompanyId_ShouldReturnList() throws Exception {
        when(workGroupService.findByCompanyId(companyId)).thenReturn(List.of(workGroupDTO));

        mockMvc.perform(get("/api/v1/work-groups/company/{companyId}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Desarrollo Backend"))
                .andExpect(jsonPath("$[0].company.id").value(companyId.toString()));

        verify(workGroupService, times(1)).findByCompanyId(companyId);
    }

    @Test
    void updateWorkGroup_ShouldReturnUpdated_WhenExists() throws Exception {
        when(workGroupService.update(eq(workGroupId), any(WorkGroupRequestDTO.class))).thenReturn(workGroupDTO);

        mockMvc.perform(put("/api/v1/work-groups/{id}", workGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workGroupRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Desarrollo Backend"));

        verify(workGroupService, times(1)).update(eq(workGroupId), any(WorkGroupRequestDTO.class));
    }

    @Test
    void deleteWorkGroup_ShouldReturnNoContent_WhenExists() throws Exception {
        doNothing().when(workGroupService).delete(workGroupId);

        mockMvc.perform(delete("/api/v1/work-groups/{id}", workGroupId))
                .andExpect(status().isNoContent());

        verify(workGroupService, times(1)).delete(workGroupId);
    }
}