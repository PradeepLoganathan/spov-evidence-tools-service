package com.pradeepl.evidence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EvidenceToolsEndpoint File-based Log and Metrics Analysis Tests")
public class EvidenceToolsEndpointTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final EvidenceToolsEndpoint endpoint = new EvidenceToolsEndpoint();

    @Test
    @DisplayName("Should fetch and analyze payment service logs")
    public void testPaymentServiceLogs() throws Exception {
        String result = endpoint.fetchLogs("payment-service", 10);

        System.out.println("=== PAYMENT SERVICE LOGS ===");
        System.out.println(result);
        System.out.println();

        // Parse JSON response
        JsonNode response = mapper.readTree(result);

        // Verify the response structure
        assertThat(response.has("logs")).isTrue();
        assertThat(response.has("service")).isTrue();
        assertThat(response.has("analysis")).isTrue();
        assertThat(response.get("service").asText()).isEqualTo("payment-service");

        // Verify analysis structure
        JsonNode analysis = response.get("analysis");
        assertThat(analysis.has("errorCount")).isTrue();
        assertThat(analysis.has("errorPatterns")).isTrue();
        assertThat(analysis.has("sampleErrorLines")).isTrue();

        // Logs should contain relevant content
        String logs = response.get("logs").asText();
        assertThat(logs).isNotEmpty();
    }

    @Test
    @DisplayName("Should fetch and analyze user service logs")
    public void testUserServiceLogs() throws Exception {
        String result = endpoint.fetchLogs("user-service", 15);

        System.out.println("=== USER SERVICE LOGS ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);

        assertThat(response.get("service").asText()).isEqualTo("user-service");
        assertThat(response.get("linesReturned").asInt()).isLessThanOrEqualTo(15);

        // Should have analysis data
        JsonNode analysis = response.get("analysis");
        assertThat(analysis).isNotNull();
    }

    @Test
    @DisplayName("Should handle non-existent service gracefully")
    public void testNonExistentService() throws Exception {
        String result = endpoint.fetchLogs("non-existent-service", 5);

        System.out.println("=== NON-EXISTENT SERVICE TEST ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);
        assertThat(response.has("error")).isTrue();
        assertThat(response.get("error").asText()).contains("No log file found for service: non-existent-service");
    }

    @Test
    @DisplayName("Should query error metrics")
    public void testErrorMetrics() throws Exception {
        String result = endpoint.queryMetrics("error_rate", "1h");

        System.out.println("=== ERROR METRICS ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);

        assertThat(response.has("formatted")).isTrue();
        assertThat(response.has("insights")).isTrue();
        assertThat(response.get("expr").asText()).isEqualTo("error_rate");

        String formatted = response.get("formatted").asText();
        assertThat(formatted).containsAnyOf("Error Rate", "Total Errors");
    }

    @Test
    @DisplayName("Should query latency metrics")
    public void testLatencyMetrics() throws Exception {
        String result = endpoint.queryMetrics("latency", "30m");

        System.out.println("=== LATENCY METRICS ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);

        assertThat(response.has("formatted")).isTrue();
        String formatted = response.get("formatted").asText();
        assertThat(formatted).containsAnyOf("Latency P95", "Average Latency");
    }

    @Test
    @DisplayName("Should query resource metrics")
    public void testResourceMetrics() throws Exception {
        String result = endpoint.queryMetrics("cpu_usage", "15m");

        System.out.println("=== RESOURCE METRICS ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);

        String formatted = response.get("formatted").asText();
        assertThat(formatted).containsAnyOf("CPU Usage", "Memory");
    }

    @Test
    @DisplayName("Should query throughput metrics")
    public void testThroughputMetrics() throws Exception {
        String result = endpoint.queryMetrics("throughput", "1h");

        System.out.println("=== THROUGHPUT METRICS ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);

        String formatted = response.get("formatted").asText();
        assertThat(formatted).containsAnyOf("Request Rate", "Success Rate");
    }

    @Test
    @DisplayName("Should analyze order service logs and detect patterns")
    public void testOrderServiceLogAnalysis() throws Exception {
        String result = endpoint.fetchLogs("order-service", 20);

        System.out.println("=== ORDER SERVICE LOG ANALYSIS ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);
        assertThat(response.get("service").asText()).isEqualTo("order-service");

        // Should have analysis data
        JsonNode analysis = response.get("analysis");
        assertThat(analysis.has("errorCount")).isTrue();
        assertThat(analysis.has("errorPatterns")).isTrue();
    }

    @Test
    @DisplayName("Should correlate evidence from logs and metrics")
    public void testCorrelateEvidence() throws Exception {
        String result = endpoint.correlateEvidence(
            "High rate of HTTP 503 errors and database timeouts",
            "CPU spike to 95% at 14:30 UTC, memory at 87%"
        );

        System.out.println("=== CORRELATE EVIDENCE ===");
        System.out.println(result);
        System.out.println();

        JsonNode response = mapper.readTree(result);

        assertThat(response.has("logFindings")).isTrue();
        assertThat(response.has("metricFindings")).isTrue();
        assertThat(response.has("potentialCorrelations")).isTrue();
        assertThat(response.has("confidence")).isTrue();

        assertThat(response.get("logFindings").asText()).contains("HTTP 503 errors");
        assertThat(response.get("metricFindings").asText()).contains("CPU spike");
    }
}
