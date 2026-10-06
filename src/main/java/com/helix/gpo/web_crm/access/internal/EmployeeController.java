package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.dto.AccessDtos;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.CreateEmployeeRequest;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.EmployeeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crm/employees")
class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.created(URI.create("/api/crm/employees/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    EmployeeResponse update(@PathVariable UUID id, @Valid @RequestBody AccessDtos.UpdateEmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/projects/{projectId}")
    EmployeeResponse assignProject(
            @PathVariable UUID id,
            @PathVariable UUID projectId,
            @Valid @RequestBody AccessDtos.AssignProjectRequest request
    ) {
        return employeeService.assignProject(id, projectId, request.tenantId());
    }

    @DeleteMapping("/{id}/projects/{projectId}")
    EmployeeResponse unassignProject(@PathVariable UUID id, @PathVariable UUID projectId) {
        return employeeService.unassignProject(id, projectId);
    }

    @PostMapping("/{id}/deactivate")
    EmployeeResponse deactivate(@PathVariable UUID id) {
        return employeeService.deactivate(id);
    }

    @PostMapping("/{id}/activate")
    EmployeeResponse activate(@PathVariable UUID id) {
        return employeeService.activate(id);
    }

    @GetMapping
    List<EmployeeResponse> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    EmployeeResponse findById(@PathVariable UUID id) {
        return employeeService.findById(id);
    }

}
