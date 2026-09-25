package de.hunar.insurance.claim.controller;

import de.hunar.insurance.claim.mapper.ClaimMapper;
import de.hunar.insurance.claim.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import java.util.List;

@WebMvcTest(ClaimController.class)
@Import({ClaimMapper.class, de.hunar.insurance.shared.security.SecurityConfig.class})
class ClaimControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClaimService claimService;

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/claims")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "clerk", roles = "CLERK")
    void authenticatedClerkMayReadClaims() throws Exception {
        when(claimService.getAllClaims()).thenReturn(List.of());
        mockMvc.perform(get("/api/claims")).andExpect(status().isOk());
    }
}
