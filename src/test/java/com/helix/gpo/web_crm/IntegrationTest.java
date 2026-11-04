package com.helix.gpo.web_crm;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// full application against a real mysql - no cognito, no aws calls
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    // the real decoder fetches cognito's keys over the network at startup,
    // tests authenticate via SecurityMockMvcRequestPostProcessors.jwt() instead
    @MockitoBean
    protected JwtDecoder jwtDecoder;

}
