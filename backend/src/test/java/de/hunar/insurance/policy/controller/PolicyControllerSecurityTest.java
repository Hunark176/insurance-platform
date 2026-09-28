package de.hunar.insurance.policy.controller;

import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.dto.PolicyResponse;
import de.hunar.insurance.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
@Import(SecurityConfig.class)
class PolicyControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolicyService policyService;

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/policies")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "clerk", roles = "CLERK")
    void authenticatedUserMayReadAllPolicies() throws Exception {
        when(policyService.findAllResponses()).thenReturn(List.of(
                new PolicyResponse(1L, 2L, 3L, null, null, null, null, null)));

        mockMvc.perform(get("/api/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(2))
                .andExpect(jsonPath("$[0].productId").value(3));
    }
}
