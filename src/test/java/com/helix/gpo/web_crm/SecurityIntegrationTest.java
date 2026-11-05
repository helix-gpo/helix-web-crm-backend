package com.helix.gpo.web_crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// every test runs in a transaction that is rolled back - fixtures and created data disappear afterwards
@Transactional
@Sql("/sql/access-fixtures.sql")
class SecurityIntegrationTest extends IntegrationTest {

    private static final String VALID_TENANT = """
            {"companyName": "Testfirma GmbH", "referenceCode": "TF"}
            """;

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor user(String cognitoSub) {
        return jwt().jwt(token -> token.subject(cognitoSub));
    }

    // ---- authentication ----

    @Test
    void crmApiWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/crm/tenants")).andExpect(status().isUnauthorized());
    }

    @Test
    void cognitoUserWithoutEmployeeIsForbidden() throws Exception {
        mockMvc.perform(get("/api/crm/tenants").with(user("unknown-sub"))).andExpect(status().isForbidden());
    }

    @Test
    void deactivatedEmployeeIsLoggedOut() throws Exception {
        mockMvc.perform(get("/api/crm/tenants").with(jwt().jwt(j -> j.subject("inactive-sub"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DEACTIVATED"));
    }

    // ---- permissions ----

    @Test
    void readerMayReadTenants() throws Exception {
        mockMvc.perform(get("/api/crm/tenants").with(user("reader-sub"))).andExpect(status().isOk());
    }

    @Test
    void readerMayNotCreateTenants() throws Exception {
        mockMvc.perform(post("/api/crm/tenants").with(user("reader-sub"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_TENANT))
                .andExpect(status().isForbidden());
    }

    @Test
    void readerMayNotSeeProjectsWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/crm/projects").with(user("reader-sub"))).andExpect(status().isForbidden());
    }

    @Test
    void adminMayCreateTenants() throws Exception {
        mockMvc.perform(post("/api/crm/tenants").with(user("admin-sub"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_TENANT))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void tooLongInputIsRejectedWithBadRequest() throws Exception {
        String tooLong = """
                {"companyName": "%s", "referenceCode": "TF"}
                """.formatted("A".repeat(200));

        mockMvc.perform(post("/api/crm/tenants").with(user("admin-sub"))
                        .contentType(MediaType.APPLICATION_JSON).content(tooLong))
                .andExpect(status().isBadRequest());
    }

    // ---- administration ----

    @Test
    void onlyAdminsMayManageEmployees() throws Exception {
        mockMvc.perform(get("/api/crm/employees").with(user("reader-sub"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/crm/employees").with(user("admin-sub"))).andExpect(status().isOk());
    }

    // ---- public and fallback paths ----

    @Test
    void publicWebsiteApiNeedsNoToken() throws Exception {
        mockMvc.perform(get("/api/public/projects")).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/testimonials")).andExpect(status().isOk());
    }

    @Test
    void healthIsOpenButOtherActuatorEndpointsAreNot() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/env")).andExpect(status().isForbidden());
    }

    @Test
    void unknownPathsAreDenied() throws Exception {
        mockMvc.perform(get("/irgendwas")).andExpect(status().isForbidden());
    }

}
