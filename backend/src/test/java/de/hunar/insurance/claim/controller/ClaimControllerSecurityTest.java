package de.hunar.insurance.claim.controller;

import de.hunar.insurance.claim.dto.ClaimPageResponse;
import de.hunar.insurance.claim.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@WebMvcTest(ClaimController.class)
@Import(de.hunar.insurance.shared.security.SecurityConfig.class)
class ClaimControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ClaimService claimService;

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/claims")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "clerk", roles = "CLERK")
    void authenticatedClerkMayReadClaims() throws Exception {
        when(claimService.getClaimResponses(any(Pageable.class), isNull()))
                .thenReturn(new ClaimPageResponse(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @WithMockUser(username = "clerk", roles = "CLERK")
    void policyFilterAndPageParametersAreForwarded() throws Exception {
        when(claimService.getClaimResponses(any(Pageable.class), eq(12345L)))
                .thenReturn(new ClaimPageResponse(List.of(), 1, 5, 0, 0, false, true));

        mockMvc.perform(get("/api/claims").param("page", "1").param("size", "5").param("policyId", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(claimService).getClaimResponses(argThat(pageable ->
                pageable.getPageNumber() == 1
                        && pageable.getPageSize() == 5
                        && pageable.getSort().getOrderFor("createdAt").isDescending()
                        && pageable.getSort().getOrderFor("id").isDescending()), eq(12345L));
    }

    @Test
    @WithMockUser(username = "clerk", roles = "CLERK")
    void invalidPaginationParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/claims").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/claims").param("size", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/claims").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/claims").param("policyId", "-1"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(claimService);
    }
}
