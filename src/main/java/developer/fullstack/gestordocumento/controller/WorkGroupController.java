package developer.fullstack.gestordocumento.controller;

import developer.fullstack.gestordocumento.dto.WorkGroupDTO;
import developer.fullstack.gestordocumento.dto.WorkGroupRequestDTO;
import developer.fullstack.gestordocumento.service.WorkGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-groups")
public class WorkGroupController {

    private final WorkGroupService workGroupService;

    public WorkGroupController(WorkGroupService workGroupService) {
        this.workGroupService = workGroupService;
    }

    @PostMapping
    public ResponseEntity<WorkGroupDTO> create(@RequestBody WorkGroupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workGroupService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<WorkGroupDTO>> findAll() {
        return ResponseEntity.ok(workGroupService.findAll());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<WorkGroupDTO>> findByCompanyId(@PathVariable UUID companyId) {
        return ResponseEntity.ok(workGroupService.findByCompanyId(companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkGroupDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(workGroupService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkGroupDTO> update(@PathVariable UUID id, @RequestBody WorkGroupRequestDTO dto) {
        return ResponseEntity.ok(workGroupService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        workGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
