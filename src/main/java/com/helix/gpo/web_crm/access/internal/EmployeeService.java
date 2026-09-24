package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.CreateEmployeeRequest;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.EmployeeResponse;
import com.helix.gpo.web_crm.access.internal.dto.AccessDtos.UpdateEmployeeRoleRequest;
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
    private final NotificationApi notificationApi;

    EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalStateException("Für diese E-Mail-Adresse existiert bereits ein Mitarbeiter.");
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new EntityNotFoundException("Diese Rolle wurde nicht gefunden."));

        String temporaryPassword = cognitoAdminService.createUser(request.email());
        cognitoAdminService.addToGroup(request.email(), role.isUnrestricted() ? ADMIN_GROUP : EMPLOYEE_GROUP);

        Employee employee = Employee.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(role)
                .active(true)
                .build();
        employee = employeeRepository.save(employee);

        sendInvitationEmail(employee, temporaryPassword);

        return AccessMapper.toResponse(employee, List.of());
    }

    EmployeeResponse updateRole(UUID employeeId, UpdateEmployeeRoleRequest request) {
        Employee employee = getOrThrow(employeeId);
        Role oldRole = employee.getRole();

        Role newRole = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new EntityNotFoundException("Diese Rolle wurde nicht gefunden."));

        if (oldRole.isUnrestricted() != newRole.isUnrestricted()) {
            String oldGroup = oldRole.isUnrestricted() ? ADMIN_GROUP : EMPLOYEE_GROUP;
            String newGroup = newRole.isUnrestricted() ? ADMIN_GROUP : EMPLOYEE_GROUP;
            cognitoAdminService.removeFromGroup(employee.getEmail(), oldGroup);
            cognitoAdminService.addToGroup(employee.getEmail(), newGroup);
        }

        employee.assignRole(newRole);

        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse assignProject(UUID employeeId, UUID projectId) {
        Employee employee = getOrThrow(employeeId);
        if (!assignmentRepository.existsByEmployeeIdAndProjectId(employeeId, projectId)) {
            assignmentRepository.save(EmployeeProjectAssignment.builder()
                    .employeeId(employeeId)
                    .projectId(projectId)
                    .build());
        }
        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse unassignProject(UUID employeeId, UUID projectId) {
        Employee employee = getOrThrow(employeeId);
        assignmentRepository.deleteByEmployeeIdAndProjectId(employeeId, projectId);
        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse deactivate(UUID employeeId) {
        Employee employee = getOrThrow(employeeId);
        employee.deactivate();
        cognitoAdminService.deleteUser(employee.getEmail());
        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    EmployeeResponse activate(UUID employeeId) {
        Employee employee = getOrThrow(employeeId);
        employee.activate();

        String temporaryPassword = cognitoAdminService.createUser(employee.getEmail());
        String group = employee.getRole().isUnrestricted() ? ADMIN_GROUP : EMPLOYEE_GROUP;
        cognitoAdminService.addToGroup(employee.getEmail(), group);
        sendInvitationEmail(employee, temporaryPassword);

        return AccessMapper.toResponse(employee, assignedProjectIds(employeeId));
    }

    @Transactional(readOnly = true)
    List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
                .map(employee -> AccessMapper.toResponse(employee, assignedProjectIds(employee.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    EmployeeResponse findById(UUID id) {
        Employee employee = getOrThrow(id);
        return AccessMapper.toResponse(employee, assignedProjectIds(id));
    }

    private List<UUID> assignedProjectIds(UUID employeeId) {
        return assignmentRepository.findAllByEmployeeId(employeeId).stream()
                .map(EmployeeProjectAssignment::getProjectId)
                .toList();
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
