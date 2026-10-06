package com.helix.gpo.web_crm.access.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
class CurrentUserDirectory {

    private final EmployeeRepository employeeRepository;

    private final Map<String, String> emailBySub = new ConcurrentHashMap<>();
    private final Set<String> alreadyWarned = ConcurrentHashMap.newKeySet();

    // called once per request by CurrentUserFilter, outside of any hibernate flush
    void resolve(String sub) {
        if (sub == null || emailBySub.containsKey(sub)) {
            return;
        }
        employeeRepository.findByCognitoSub(sub).ifPresentOrElse(
                employee -> emailBySub.put(sub, employee.getEmail()),
                () -> {
                    if (alreadyWarned.add(sub)) {
                        log.warn("Kein Mitarbeiter mit cognito_sub={} gefunden - Zugriff läuft über die Übergangs-Ausnahme", sub);
                    }
                });
    }

    // cache only, no database access - safe to call from hibernate callbacks
    Optional<String> emailOf(String sub) {
        return sub == null ? Optional.empty() : Optional.ofNullable(emailBySub.get(sub));
    }

    void evict(String sub) {
        if (sub != null) {
            emailBySub.remove(sub);
            alreadyWarned.remove(sub);
        }
    }

}
