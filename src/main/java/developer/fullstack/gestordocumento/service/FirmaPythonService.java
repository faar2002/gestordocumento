package developer.fullstack.gestordocumento.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class FirmaPythonService {

    private final RestTemplate restTemplate;
    
    @Value("${python.firma.service.url:http://localhost:8000/api}")
    private String pythonServiceUrl;

    public FirmaPythonService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> solicitarOtp(String uuidTransaccion) {
        String url = pythonServiceUrl + "/otp/solicitar/";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("uuid_transaccion", uuidTransaccion);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "No se pudo comunicar con el servicio de OTP en Python.");
            return errorMap;
        }
    }

    public Map<String, Object> validarOtp(String uuidTransaccion, String otpCode) {
        String url = pythonServiceUrl + "/otp/validar/";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("uuid_transaccion", uuidTransaccion);
        requestBody.put("otp_code", otpCode);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("valid", false);
            errorMap.put("message", "Código OTP inválido o error de comunicación.");
            return errorMap;
        }
    }
}