package developer.fullstack.gestordocumento.service;

import developer.fullstack.gestordocumento.dto.UserRequestDTO;
import developer.fullstack.gestordocumento.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO findById(UUID id);
    List<UserResponseDTO> findAll();
    UserResponseDTO update(UUID id, UserRequestDTO dto);
    void delete(UUID id);
}
