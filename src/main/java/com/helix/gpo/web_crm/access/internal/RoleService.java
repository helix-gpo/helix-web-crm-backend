package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.AccessApi;
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
    private final EmployeeRepository employeeRepository;
    private final AccessApi accessApi;

    RoleResponse create(CreateRoleRequest request) {
        accessApi.requireAdministration();

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
        accessApi.requireAdministration();
        Role role = getOrThrow(id);

        boolean nameTaken = roleRepository.existsByNameIgnoreCase(request.name())
                && !role.getName().equalsIgnoreCase(request.name());
        if (nameTaken) {
            throw new IllegalStateException("Eine Rolle mit diesem Namen existiert bereits.");
        }

        boolean losesUnrestricted = role.isUnrestricted() && !Boolean.TRUE.equals(request.unrestricted());
        if (losesUnrestricted
                && employeeRepository.countActiveInRole(id) > 0
                && employeeRepository.countActiveAdminsOutsideRole(id) == 0) {
            throw new IllegalStateException(
                    "Diese Rolle ist die einzige mit uneingeschränktem Zugriff. Ohne sie hätte niemand mehr Administratorrechte.");
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
        accessApi.requireAdministration();
        Role role = getOrThrow(id);

        if (employeeRepository.existsAnyWithRole(id)) {
            throw new IllegalStateException(
                    "Dieser Rolle sind noch Mitarbeiter zugewiesen. Bitte weise sie zuerst einer anderen Rolle zu.");
        }

        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    List<RoleResponse> findAll() {
        accessApi.requireAdministration();
        return roleRepository.findAll().stream()
                .map(AccessMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    RoleResponse findById(UUID id) {
        accessApi.requireAdministration();
        return AccessMapper.toResponse(getOrThrow(id));
    }

    private Role getOrThrow(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Diese Rolle wurde nicht gefunden."));
    }

}
