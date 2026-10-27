package developer.fullstack.gestordocumento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import developer.fullstack.gestordocumento.entity.TransaccionFirmaEntity;
import developer.fullstack.gestordocumento.repository.TransaccionFirmaRepository;
import developer.fullstack.gestordocumento.service.FirmaPythonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = FirmaController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class FirmaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransaccionFirmaRepository transaccionRepository;

    @MockBean
    private FirmaPythonService firmaPythonService;

    private UUID transactionUuid;
    private TransaccionFirmaEntity transaccionEntity;

    @BeforeEach
    void setUp() {
        transactionUuid = UUID.randomUUID();
        transaccionEntity = new TransaccionFirmaEntity();
        transaccionEntity.setUuid(transactionUuid);
        transaccionEntity.setNombreFirmante("Francisco Aponte");
        transaccionEntity.setEmailFirmante("faponte@yopmail.com");
        transaccionEntity.setEstado("PENDIENTE");
    }

    @Test
    void obtenerTransaccion_ShouldReturnEntity_WhenExists() throws Exception {
        when(transaccionRepository.findByDocumentId(transactionUuid)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/firmas/transaccion/{uuid}", transactionUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreFirmante").value("Francisco Aponte"))
                .andExpect(jsonPath("$.emailFirmante").value("faponte@yopmail.com"));

        verify(transaccionRepository, times(1)).findByDocumentId(transactionUuid);
    }

    @Test
    void solicitarOtp_ShouldReturnPythonResponse() throws Exception {
        Map<String, Object> mockResponse = Map.of("status", "success", "message", "Código OTP enviado correctamente");
        when(firmaPythonService.solicitarOtp(anyString())).thenReturn(mockResponse);

        Map<String, String> requestBody = Map.of("uuid_transaccion", transactionUuid.toString());

        mockMvc.perform(post("/api/v1/firmas/otp/solicitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(firmaPythonService, times(1)).solicitarOtp(anyString());
    }
}