package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.dto.AccessDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public AccessDtos.MeResponse me(Jwt jwt) {
        return employeeRepository.findByCognitoSub(jwt.getSubject())
                .filter(Employee::isActive)
                .map(employee -> new AccessDtos.MeResponse(
                        employee.getEmail(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getRole().getName(),
                        employee.getRole().isUnrestricted(),
                        employee.getRole().getPermissions().stream()
                                .map(p -> new AccessDtos.PermissionEntryDto(p.entityType(), p.action()))
                                .toList()))
                .orElse(new AccessDtos.MeResponse(null, null, null, null, false, List.of()));
    }

}
