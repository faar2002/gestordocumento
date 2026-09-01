package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.dto.AuthResponseDTO;
import developer.fullstack.gestordocumento.dto.LoginRequestDTO;
import developer.fullstack.gestordocumento.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthService authService;

    public AuthenticationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}