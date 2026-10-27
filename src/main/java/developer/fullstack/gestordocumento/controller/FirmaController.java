package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.entity.TransaccionFirmaEntity;
import developer.fullstack.gestordocumento.repository.TransaccionFirmaRepository;
import developer.fullstack.gestordocumento.service.FirmaPythonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/firmas")
public class FirmaController {

    private final TransaccionFirmaRepository transaccionRepository;
    private final FirmaPythonService firmaPythonService;

    public FirmaController(TransaccionFirmaRepository transaccionRepository, FirmaPythonService firmaPythonService) {
        this.transaccionRepository = transaccionRepository;
        this.firmaPythonService = firmaPythonService;
    }

    @GetMapping("/transaccion/{uuid}")
    public ResponseEntity<TransaccionFirmaEntity> obtenerTransaccion(@PathVariable UUID uuid) {
        TransaccionFirmaEntity transaccion = transaccionRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Transacción de firma no encontrada"));
        return ResponseEntity.ok(transaccion);
    }

    @PostMapping("/otp/solicitar")
    public ResponseEntity<Map<String, Object>> solicitarOtp(@RequestBody Map<String, String> payload) {
        String uuidTransaccion = payload.get("uuid_transaccion");
        Map<String, Object> resultado = firmaPythonService.solicitarOtp(uuidTransaccion);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/otp/validar")
    public ResponseEntity<Map<String, Object>> validarOtp(@RequestBody Map<String, String> payload) {
        String uuidTransaccion = payload.get("uuid_transaccion");
        String otpCode = payload.get("otp_code");
        
        Map<String, Object> resultado = firmaPythonService.validarOtp(uuidTransaccion, otpCode);
        return ResponseEntity.ok(resultado);
    }
}