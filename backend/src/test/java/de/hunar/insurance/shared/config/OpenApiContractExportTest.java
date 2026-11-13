package de.hunar.insurance.shared.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exports the OpenAPI contract that the frontend types are generated from.
 *
 * <p>The contract is always written to {@code target/openapi.json}. With
 * {@code -Dopenapi.export=true} it is also written to {@code ../frontend/openapi.json};
 * {@code npm run generate:api-types} then regenerates {@code src/api/generated/schema.ts}.
 * CI runs both steps and fails if the committed files differ, so backend and frontend types
 * cannot drift apart unnoticed.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false"
})
class OpenApiContractExportTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportsTheOpenApiContract() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode contract = objectMapper.readTree(json);
        // OpenAPI 3.1 expresses "may be null" as a type array; generated types then allow null.
        assertThat(contract.at("/components/schemas/PolicyResponse/properties/cancelledOn/type").toString())
                .as("nullable fields must be marked so generated types allow null")
                .contains("\"null\"");

        String pretty = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(contract) + "\n";
        Files.writeString(Path.of("target", "openapi.json"), pretty);
        if (Boolean.getBoolean("openapi.export")) {
            Files.writeString(Path.of("..", "frontend", "openapi.json"), pretty);
        }
    }
}
