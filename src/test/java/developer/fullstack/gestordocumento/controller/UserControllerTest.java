package developer.fullstack.gestordocumento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import developer.fullstack.gestordocumento.dto.UserRequestDTO;
import developer.fullstack.gestordocumento.dto.UserResponseDTO;
import developer.fullstack.gestordocumento.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UUID userId;
    private UUID companyId;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        // Inicializamos el record UserRequestDTO con sus respectivos parámetros
        userRequestDTO = new UserRequestDTO(
                "Francisco",
                "Antonio",
                "Aponte",
                "Rivas",
                "faponte@yopmail.com",
                "securePassword123",
                companyId,
                Set.of(UUID.randomUUID()),
                Set.of(UUID.randomUUID()),
                Set.of(UUID.randomUUID())
        );

        // Inicializamos el record UserResponseDTO con sus respectivos parámetros
        userResponseDTO = new UserResponseDTO(
                userId,
                "Francisco",
                "Antonio",
                "Aponte",
                "Rivas",
                "Francisco Antonio Aponte Rivas",
                "faponte@yopmail.com",
                true,
                null,
                Set.of("DOC_SYS"),
                Set.of(),
                Set.of()
        );
    }

    @Test
    void create_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        when(userService.create(any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Francisco"))
                .andExpect(jsonPath("$.email").value("faponte@yopmail.com"));

        verify(userService, times(1)).create(any(UserRequestDTO.class));
    }

    @Test
    void findAll_ShouldReturnList_WhenUsersExist() throws Exception {
        when(userService.findAll()).thenReturn(List.of(userResponseDTO));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Francisco"))
                .andExpect(jsonPath("$[0].email").value("faponte@yopmail.com"));

        verify(userService, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() throws Exception {
        when(userService.findById(userId)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Francisco"));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    void updateByEmailAndCompany_ShouldReturnUpdated_WhenValid() throws Exception {
        when(userService.updateByEmailAndCompany(eq("faponte@yopmail.com"), eq(companyId), any(UserRequestDTO.class)))
                .thenReturn(userResponseDTO);

        mockMvc.perform(put("/api/v1/users")
                        .param("email", "faponte@yopmail.com")
                        .param("companyId", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("faponte@yopmail.com"));

        verify(userService, times(1)).updateByEmailAndCompany(eq("faponte@yopmail.com"), eq(companyId), any(UserRequestDTO.class));
    }

    @Test
    void delete_ShouldReturnNoContent_WhenExists() throws Exception {
        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(userId);
    }

    @Test
    void toggleStatus_ShouldUpdateStatus_WhenValid() throws Exception {
        when(userService.toggleUserStatus(userId, true)).thenReturn(userResponseDTO);

        mockMvc.perform(patch("/api/v1/users/{id}/status", userId)
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userService, times(1)).toggleUserStatus(userId, true);
    }
}