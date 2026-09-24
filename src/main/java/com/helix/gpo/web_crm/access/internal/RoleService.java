package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.CreateRoleRequest;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.RoleResponse;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.UpdateRoleRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class RoleService {

    private final RoleRepository roleRepository;

    RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalStateException("Eine Rolle mit diesem Namen existiert bereits.");
        }

        Role role = Role.builder()
                .name(request.name())
                .description(request.description())
                .unrestricted(Boolean.TRUE.equals(request.unrestricted()))
                .permissions(AccessMapper.toPermissionSet(request.permissions()))
                .build();

        return AccessMapper.toResponse(roleRepository.save(role));
    }

    RoleResponse update(UUID id, UpdateRoleRequest request) {
        Role role = getOrThrow(id);

        boolean nameTaken = roleRepository.existsByNameIgnoreCase(request.name())
                && !role.getName().equalsIgnoreCase(request.name());
        if (nameTaken) {
            throw new IllegalStateException("Eine Rolle mit diesem Namen existiert bereits.");
        }

        role.updateDetails(
                request.name(),
                request.description(),
                Boolean.TRUE.equals(request.unrestricted()),
                AccessMapper.toPermissionSet(request.permissions())
        );

        return AccessMapper.toResponse(role);
    }

    void delete(UUID id) {
        Role role = getOrThrow(id);
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(AccessMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    RoleResponse findById(UUID id) {
        return AccessMapper.toResponse(getOrThrow(id));
    }

    private Role getOrThrow(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Diese Rolle wurde nicht gefunden."));
    }

}
