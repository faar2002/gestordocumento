package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.dto.RoleDTO;
import developer.fullstack.gestordocumento.dto.RoleRequestDTO;
import developer.fullstack.gestordocumento.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<RoleDTO> create(@Valid @RequestBody RoleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<RoleDTO>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.findById(id));
    }
}