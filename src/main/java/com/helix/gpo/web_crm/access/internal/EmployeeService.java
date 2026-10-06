package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.AccessApi;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.CreateEmployeeRequest;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.EmployeeResponse;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.UpdateEmployeeRequest;
import com.helix.gpo.web_crm.notification.EmailMessage;
import com.helix.gpo.web_crm.notification.NotificationApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class EmployeeService {

    private static final String ADMIN_GROUP = "admin";
    private static final String EMPLOYEE_GROUP = "employee";

    private final EmployeeRepository employeeRepository;
    private final EmployeeProjectAssignmentRepository assignmentRepository;
    private final RoleRepository roleRepository;
    private final CognitoAdminService cognitoAdminService;
    private final CurrentUserDirectory currentUserDirectory;
    private final NotificationApi notificationApi;
    private final AccessApi accessApi;

    EmployeeResponse create(CreateEmployeeRequest request) {
        accessApi.requireAdministration();

        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalStateException("Für diese E-Mail-Adresse existiert bereits ein Mitarbeiter.");
        }

        Role role = getRoleOrThrow(request.roleId());

        CognitoAdminService.CreatedCognitoUser cognitoUser = cognitoAdminService.createUser(request.email());
        cognitoAdminService.addToGroup(request.email(), groupFor(role));

        Employee employee = Employee.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(role)
                .cognitoSub(cognitoUser.sub())
                .active(true)
                .build();
        employee = employeeRepository.save(employee);

        sendInvitationEmail(employee, cognitoUser.temporaryPassword());

        return AccessMapper.toResponse(employee, List.of());
    }

    EmployeeResponse update(UUID employeeId, UpdateEmployeeRequest request) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(employeeId);

        Role oldRole = employee.getRole();
        Role newRole = getRoleOrThrow(request.roleId());

        if (oldRole.isUnrestricted() && !newRole.isUnrestricted()) {
            assertNotLastAdmin(employee);
        }

        employee.updateDetails(request.firstName(), request.lastName());

        if (oldRole.isUnrestricted() != newRole.isUnrestricted()) {
            cognitoAdminService.removeFromGroup(employee.getEmail(), groupFor(oldRole));
            cognitoAdminService.addToGroup(employee.getEmail(), groupFor(newRole));
        }

        employee.assignRole(newRole);

        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse assignProject(UUID employeeId, UUID projectId, UUID tenantId) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(employeeId);

        if (!assignmentRepository.existsByEmployeeIdAndProjectId(employeeId, projectId)) {
            assignmentRepository.save(EmployeeProjectAssignment.builder()
                    .employeeId(employeeId)
                    .projectId(projectId)
                    .tenantId(tenantId)
                    .build());
        }
        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse unassignProject(UUID employeeId, UUID projectId) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(employeeId);

        assignmentRepository.deleteByEmployeeIdAndProjectId(employeeId, projectId);
        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    // locks the cognito user instead of deleting it: reversible, keeps password and sub
    EmployeeResponse deactivate(UUID employeeId) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(employeeId);

        assertNotSelf(employee, "deaktivieren");
        assertNotLastAdmin(employee);

        employee.deactivate();
        cognitoAdminService.disableUser(employee.getEmail());

        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse activate(UUID employeeId) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(employeeId);

        employee.activate();

        if (!cognitoAdminService.enableUser(employee.getEmail())) {
            // legacy case: the cognito user was deleted by the former deactivate flow, create it again
            CognitoAdminService.CreatedCognitoUser cognitoUser = cognitoAdminService.createUser(employee.getEmail());
            employee.linkCognitoUser(cognitoUser.sub());
            cognitoAdminService.addToGroup(employee.getEmail(), groupFor(employee.getRole()));
            sendInvitationEmail(employee, cognitoUser.temporaryPassword());
        }

        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    void delete(UUID employeeId) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(employeeId);

        assertNotSelf(employee, "löschen");
        assertNotLastAdmin(employee);

        cognitoAdminService.deleteUser(employee.getEmail());
        currentUserDirectory.evict(employee.getCognitoSub());
        assignmentRepository.deleteAll(assignmentRepository.findAllByEmployeeId(employeeId));
        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    List<EmployeeResponse> findAll() {
        accessApi.requireAdministration();
        return employeeRepository.findAll().stream()
                .map(employee -> AccessMapper.toResponse(employee, assignedProjectIds(employee.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    EmployeeResponse findById(UUID id) {
        accessApi.requireAdministration();
        Employee employee = getOrThrow(id);
        return AccessMapper.toResponse(employee, assignedProjectIds(id));
    }

    private void assertNotSelf(Employee employee, String action) {
        if (employee.getEmail().equalsIgnoreCase(accessApi.currentUserEmail())) {
            throw new IllegalStateException("Du kannst deinen eigenen Zugang nicht " + action + ".");
        }
    }

    private void assertNotLastAdmin(Employee employee) {
        boolean isActiveAdmin = employee.isActive() && employee.getRole().isUnrestricted();
        if (isActiveAdmin && employeeRepository.countActiveAdmins() <= 1) {
            throw new IllegalStateException("Es muss mindestens ein aktiver Administrator bestehen bleiben.");
        }
    }

    private String groupFor(Role role) {
        return role.isUnrestricted() ? ADMIN_GROUP : EMPLOYEE_GROUP;
    }

    private List<UUID> assignedProjectIds(UUID employeeId) {
        return assignmentRepository.findAllByEmployeeId(employeeId).stream()
                .map(EmployeeProjectAssignment::getProjectId)
                .toList();
    }

    private Role getRoleOrThrow(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Diese Rolle wurde nicht gefunden."));
    }

    private Employee getOrThrow(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dieser Mitarbeiter wurde nicht gefunden."));
    }

    private void sendInvitationEmail(Employee employee, String temporaryPassword) {
        String subject = "Dein Zugang zum Helix GPO CRM";
        String preheader = "Dein CRM-Zugang wurde eingerichtet.";
        String body = """
                <p style="margin:0 0 16px;">Hallo %s,</p>
                <p style="margin:0 0 16px;">du hast ab sofort Zugang zum Helix GPO CRM.</p>
                <p style="margin:0 0 16px;">Login: <strong>%s</strong><br/>Temporäres Passwort: <strong>%s</strong></p>
                <p style="margin:0 0 16px;">Beim ersten Login wirst du gebeten, ein eigenes Passwort zu vergeben.</p>
                """.formatted(employee.getFirstName(), employee.getEmail(), temporaryPassword);

        notificationApi.send(new EmailMessage(employee.getEmail(), subject, preheader, body));
    }

}
