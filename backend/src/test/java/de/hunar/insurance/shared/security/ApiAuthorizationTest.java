package de.hunar.insurance.shared.security;

import de.hunar.insurance.billing.application.BillingService;
import de.hunar.insurance.billing.controller.BillingController;
import de.hunar.insurance.claim.application.ClaimService;
import de.hunar.insurance.claim.controller.ClaimController;
import de.hunar.insurance.claim.domain.ClaimStatus;
import de.hunar.insurance.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the role model with the configured demo accounts and real HTTP Basic credentials.
 */
@WebMvcTest({ClaimController.class, BillingController.class, AuthController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ApiAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ClaimService claimService;
    @MockitoBean
    private BillingService billingService;

    @Test
    void anonymousStatusChangeIsRejectedWithUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "APPROVED"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));

        verifyNoInteractions(claimService);
    }

    @Test
    void ajaxRequestsDoNotTriggerTheBrowserLoginDialog() throws Exception {
        mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "APPROVED")
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "APPROVED")
                        .with(httpBasic("clerk", "wrong")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(claimService);
    }

    @Test
    void customerMayNotChangeClaimStatus() throws Exception {
        mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "APPROVED")
                        .with(httpBasic("customer", "customer")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }

    @Test
    void clerkMayChangeClaimStatus() throws Exception {
        mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "IN_REVIEW")
                        .with(httpBasic("clerk", "clerk")))
                .andExpect(status().isOk());

        verify(claimService).updateClaimResponse(1L, ClaimStatus.IN_REVIEW);
    }

    @Test
    void customerMayReportClaim() throws Exception {
        String body = """
                {"policyId": 1, "occurredOn": "%s", "customerNumber": "K-1",
                 "claimType": "AUTO", "amount": 100.00}
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/claims").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(httpBasic("customer", "customer")))
                .andExpect(status().isCreated());

        verify(claimService).createClaimResponse(any());
    }

    @Test
    void anonymousCannotReportClaim() throws Exception {
        mockMvc.perform(post("/api/claims").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(claimService);
    }

    @Test
    void onlyAdminMayExecutePayouts() throws Exception {
        mockMvc.perform(post("/api/payouts/1/pay").with(httpBasic("clerk", "clerk")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(billingService);

        mockMvc.perform(post("/api/payouts/1/pay").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
        verify(billingService).markPaid(1L);
    }

    @Test
    void payoutsRemainPubliclyReadable() throws Exception {
        mockMvc.perform(get("/api/payouts"))
                .andExpect(status().isOk());
    }

    @Test
    void meReturnsTheAuthenticatedUserAndRoles() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("clerk", "clerk")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("clerk"))
                .andExpect(jsonPath("$.roles[0]").value("CLERK"));
    }

    @Test
    void meNamesTheCustomerOfACustomerLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("customer", "customer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value("DEMO-1001"));
        mockMvc.perform(get("/api/auth/me").with(httpBasic("clerk", "clerk")))
                .andExpect(jsonPath("$.customerNumber").doesNotExist());
    }

    @Test
    void listsRejectInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/payouts").param("size", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/payouts").param("page", "-1")).andExpect(status().isBadRequest());
        verifyNoInteractions(billingService);
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void demoAccountsArePublishedForAnonymousVisitors() throws Exception {
        mockMvc.perform(get("/api/auth/demo-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.username == 'clerk')].role").value("CLERK"));
    }
}
