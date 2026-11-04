package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessApiImplTest {

    private static final String SUB = "cognito-sub-1";

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeProjectAssignmentRepository assignmentRepository;

    @Mock
    private CurrentUserDirectory currentUserDirectory;

    @InjectMocks
    private AccessApiImpl accessApi;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ---- helpers ----

    private void loginAs(String sub) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(sub).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private void loginAsEmployee(Role role, boolean active) {
        loginAs(SUB);
        Employee employee = Employee.builder()
                .email("mitarbeiter@helix-gpo.com")
                .cognitoSub(SUB)
                .role(role)
                .active(active)
                .build();
        when(employeeRepository.findByCognitoSub(SUB)).thenReturn(Optional.of(employee));
    }

    private static Role adminRole() {
        return Role.builder().name("Administrator").unrestricted(true).build();
    }

    private static Role invoiceReaderRole() {
        return Role.builder()
                .name("Buchhaltung")
                .permissions(Set.of(new RolePermission(EntityType.INVOICE, PermissionAction.READ)))
                .build();
    }

    private void assertDeniesEverything() {
        assertThat(accessApi.isUnrestricted()).isFalse();
        assertThat(accessApi.canRead(EntityType.TENANT)).isFalse();
        assertThat(accessApi.canWrite(EntityType.INVOICE)).isFalse();
        assertThat(accessApi.accessibleProjectIds()).isEmpty();
        assertThat(accessApi.accessibleTenantIds()).isEmpty();
        assertThat(accessApi.canAccessProject(UUID.randomUUID())).isFalse();
        assertThatThrownBy(() -> accessApi.requireRead(EntityType.PROJECT)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(accessApi::requireAdministration).isInstanceOf(AccessDeniedException.class);
    }

    // ---- fail closed ----

    @Nested
    class FailClosed {

        @Test
        void anonymousRequestIsDeniedEverything() {
            assertDeniesEverything();
            assertThat(accessApi.currentUserEmail()).isNull();
        }

        @Test
        void cognitoUserWithoutEmployeeIsDeniedEverything() {
            loginAs("unknown-sub");
            when(employeeRepository.findByCognitoSub("unknown-sub")).thenReturn(Optional.empty());

            assertDeniesEverything();
        }

        @Test
        void deactivatedEmployeeLosesAccessEvenAsAdmin() {
            loginAsEmployee(adminRole(), false);

            assertDeniesEverything();
        }

    }

    // ---- roles ----

    @Nested
    class Roles {

        @Test
        void adminMayDoEverythingAndSeesAllScopes() {
            loginAsEmployee(adminRole(), true);

            assertThat(accessApi.isUnrestricted()).isTrue();
            for (EntityType entity : EntityType.values()) {
                assertThat(accessApi.canRead(entity)).isTrue();
                assertThat(accessApi.canWrite(entity)).isTrue();
                assertThat(accessApi.canDelete(entity)).isTrue();
            }
            assertThat(accessApi.canAccessProject(UUID.randomUUID())).isTrue();
            assertThat(accessApi.canAccessTenant(UUID.randomUUID())).isTrue();
            assertThatCode(accessApi::requireAdministration).doesNotThrowAnyException();
        }

        @Test
        void restrictedRoleGetsOnlyItsPermissions() {
            loginAsEmployee(invoiceReaderRole(), true);

            assertThat(accessApi.canRead(EntityType.INVOICE)).isTrue();
            assertThat(accessApi.canWrite(EntityType.INVOICE)).isFalse();
            assertThat(accessApi.canDelete(EntityType.INVOICE)).isFalse();
            assertThat(accessApi.canRead(EntityType.TENANT)).isFalse();

            assertThatCode(() -> accessApi.requireRead(EntityType.INVOICE)).doesNotThrowAnyException();
            assertThatThrownBy(() -> accessApi.requireWrite(EntityType.INVOICE)).isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(accessApi::requireAdministration).isInstanceOf(AccessDeniedException.class);
        }

    }

    // ---- scope ----

    @Nested
    class Scope {

        private final UUID assignedProject = UUID.randomUUID();
        private final UUID secondAssignedProject = UUID.randomUUID();
        private final UUID assignedTenant = UUID.randomUUID();

        private void withAssignments() {
            loginAsEmployee(invoiceReaderRole(), true);
            when(assignmentRepository.findAllByEmployeeId(any())).thenReturn(List.of(
                    EmployeeProjectAssignment.builder()
                            .projectId(assignedProject).tenantId(assignedTenant).build(),
                    EmployeeProjectAssignment.builder()
                            .projectId(secondAssignedProject).tenantId(assignedTenant).build()));
        }

        @Test
        void restrictedEmployeeSeesOnlyAssignedProjects() {
            withAssignments();

            assertThat(accessApi.accessibleProjectIds()).containsExactlyInAnyOrder(assignedProject, secondAssignedProject);
            assertThat(accessApi.canAccessProject(assignedProject)).isTrue();
            assertThat(accessApi.canAccessProject(UUID.randomUUID())).isFalse();
        }

        @Test
        void tenantsAreDerivedFromAssignmentsWithoutDuplicates() {
            withAssignments();

            assertThat(accessApi.accessibleTenantIds()).containsExactly(assignedTenant);
            assertThat(accessApi.canAccessTenant(assignedTenant)).isTrue();
            assertThat(accessApi.canAccessTenant(UUID.randomUUID())).isFalse();
        }

    }

    // ---- audit email ----

    @Test
    void currentUserEmailComesFromTheDirectoryCache() {
        loginAs(SUB);
        when(currentUserDirectory.emailOf(SUB)).thenReturn(Optional.of("felix@helix-gpo.com"));

        assertThat(accessApi.currentUserEmail()).isEqualTo("felix@helix-gpo.com");
    }

}
