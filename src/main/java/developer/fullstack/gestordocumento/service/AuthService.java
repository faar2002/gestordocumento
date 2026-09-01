package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.AuthResponseDTO;
import developer.fullstack.gestordocumento.dto.LoginRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
}