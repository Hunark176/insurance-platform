package de.hunar.insurance.audit.controller;

import de.hunar.insurance.audit.application.AuditTrailService;
import de.hunar.insurance.shared.security.SecurityConfig;
import de.hunar.insurance.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditTrailController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuditTrailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditTrailService service;

    @Test
    void anonymousCallersMayQueryAuditHistory() throws Exception {
        when(service.findForEntity(eq("CLAIM"), eq("42"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Page.empty(PageRequest.of(0, 50)));

        mockMvc.perform(get("/api/audit-trail").param("entityType", "CLAIM").param("entityId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void returnsPagedAuditEntriesForTheRequestedEntity() throws Exception {
        when(service.findForEntity(eq("CLAIM"), eq("42"), argThat(pageable ->
                pageable.getPageNumber() == 1
                        && pageable.getPageSize() == 5
                        && pageable.getSort().getOrderFor("occurredAt").isDescending())))
                .thenReturn(Page.empty(PageRequest.of(1, 5)));

        mockMvc.perform(get("/api/audit-trail")
                        .param("entityType", " claim ")
                        .param("entityId", "42")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(service).findForEntity(eq("CLAIM"), eq("42"), argThat(pageable ->
                pageable.getPageNumber() == 1 && pageable.getPageSize() == 5));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void malformedPathReturnsAControlledNotFoundResponse() throws Exception {
        mockMvc.perform(get("/api/audit-trail/status=200/durationMs=14"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Requested resource was not found"));
    }
}
