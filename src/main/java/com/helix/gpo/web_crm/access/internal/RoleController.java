package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.CreateRoleRequest;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.RoleResponse;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.UpdateRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crm/roles")
class RoleController {

    private final RoleService roleService;

    @PostMapping
    ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.create(request);
        return ResponseEntity.created(URI.create("/api/crm/roles/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    RoleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    List<RoleResponse> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    RoleResponse findById(@PathVariable UUID id) {
        return roleService.findById(id);
    }

}
