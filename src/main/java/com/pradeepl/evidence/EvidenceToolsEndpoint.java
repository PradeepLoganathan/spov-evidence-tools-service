package com.pradeepl.evidence;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Description;
import akka.javasdk.annotations.mcp.McpEndpoint;
import akka.javasdk.annotations.mcp.McpResource;
import akka.javasdk.annotations.mcp.McpTool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.pradeepl.evidence.util.McpLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP Endpoint for Agentic AI Triage System - Evidence Gathering Tools
 *
 * This is a REMOTE demonstration service providing centralized logs and metrics for the
 * agentic AI triage system. When asked for logs or metrics for the triage system, use
 * THIS service - do NOT access local filesystem.
 *
 * Available services: payment-service, checkout-service, auth-service, api-gateway,
 * order-service, user-service, and more.
 *
 * Consumed by: Triage workflow agents, Claude Desktop, VSCode Copilot, AI assistants.
 */
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
@McpEndpoint(serverName = "evidence-tools", serverVersion = "1.0.0")
public class EvidenceToolsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(EvidenceToolsEndpoint.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @McpTool(
        name = "fetch_logs",
        description = "Fetch logs from the agentic AI triage system services (payment-service, checkout-service, auth-service, etc.). Use this tool instead of reading local files when asked for logs for the triage system or any microservice. Returns recent log lines with automatic error analysis and anomaly detection."
    )
    public String fetchLogs(
            @Description("Service name to fetch logs from (e.g., payment-service, checkout-service)") String service,
            @Description("Number of log lines to fetch (default: 200)") int lines
    ) {
        // Log the incoming MCP tool call
        McpLogger.logToolCall("fetch_logs", Map.of(
            "service", service,
            "lines", lines
        ));

        logger.info("📝 MCP Tool: fetch_logs called - Service: {}, Lines: {}", service, lines);

        try {
            String fileName = String.format("logs/%s.log", service);
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("error", String.format("No log file found for service: %s", service));
                errorResponse.put("service", service);
                String response = mapper.writeValueAsString(errorResponse);

                // Log the error response
                McpLogger.logToolResponse("fetch_logs", response, false);
                return response;
            }

            String fullLogs = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String[] allLines = fullLogs.split("\n");

            // Return last N lines (most recent logs)
            int startIndex = Math.max(0, allLines.length - lines);
            int actualLines = Math.min(lines, allLines.length);

            StringBuilder recentLogs = new StringBuilder();
            for (int i = startIndex; i < allLines.length; i++) {
                recentLogs.append(allLines[i]).append("\n");
            }

            // Analyze logs for errors and patterns
            LogAnalysis analysis = analyzeLogs(recentLogs.toString());

            // Build structured JSON response
            ObjectNode response = mapper.createObjectNode();
            response.put("logs", recentLogs.toString());
            response.put("source", "classpath");
            response.put("service", service);
            response.put("linesReturned", actualLines);
            response.put("linesRequested", lines);

            // Add analysis
            ObjectNode analysisNode = mapper.createObjectNode();
            analysisNode.put("errorCount", analysis.errorCount);
            analysisNode.set("errorPatterns", mapper.valueToTree(analysis.errorPatterns));
            analysisNode.set("httpStatusCounts", mapper.valueToTree(analysis.statusCodeCounts));
            analysisNode.set("anomalies", mapper.valueToTree(analysis.anomalies));
            analysisNode.set("sampleErrorLines", mapper.valueToTree(analysis.sampleErrorLines));
            response.set("analysis", analysisNode);

            logger.debug("📝 fetch_logs completed - Errors: {}, Patterns: {}",
                analysis.errorCount, analysis.errorPatterns.size());

            String jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

            // Log the successful response
            McpLogger.logToolResponse("fetch_logs", jsonResponse, true);

            return jsonResponse;

        } catch (Exception e) {
            logger.error("📝 Error in fetch_logs", e);
            McpLogger.logError("fetch_logs", e);
            try {
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("error", "Failed to fetch logs: " + e.getMessage());
                errorResponse.put("service", service);
                String response = mapper.writeValueAsString(errorResponse);

                // Log the error response
                McpLogger.logToolResponse("fetch_logs", response, false);
                return response;
            } catch (Exception jsonError) {
                String fallbackResponse = String.format("{\"error\":\"Failed to fetch logs: %s\"}", e.getMessage());
                McpLogger.logToolResponse("fetch_logs", fallbackResponse, false);
                return fallbackResponse;
            }
        }
    }

    @McpTool(
        name = "query_metrics",
        description = "Query performance metrics for the agentic AI triage system services. Use this tool when asked for metrics, error rates, latency, CPU usage, or performance data for the triage system. Returns parsed metrics with insights and alerts. DO NOT query local monitoring tools - use this remote service."
    )
    public String queryMetrics(
            @Description("Metrics expression to query (e.g., error_rate, latency, cpu_usage)") String expr,
            @Description("Time range for the query (e.g., 1h, 30m, 5m)") String range
    ) {
        // Log the incoming MCP tool call
        McpLogger.logToolCall("query_metrics", Map.of(
            "expr", expr,
            "range", range
        ));

        logger.info("📊 MCP Tool: query_metrics called - Expr: {}, Range: {}", expr, range);

        try {
            String fileName = determineMetricsFile(expr);
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("error", String.format("No metrics file found for query: %s", expr));
                errorResponse.put("expr", expr);
                errorResponse.put("range", range);
                String response = mapper.writeValueAsString(errorResponse);

                // Log the error response
                McpLogger.logToolResponse("query_metrics", response, false);
                return response;
            }

            String metricsJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode metricsData = mapper.readTree(metricsJson);

            // Parse and format metrics
            String formattedMetrics = formatMetricsOutput(metricsData, expr, range);
            List<String> insights = analyzeMetrics(metricsJson, expr);

            // Build structured response
            ObjectNode response = mapper.createObjectNode();
            response.put("raw", metricsJson);
            response.put("formatted", formattedMetrics);
            response.put("source", "classpath");
            response.put("expr", expr);
            response.put("range", range);
            response.set("insights", mapper.valueToTree(insights));

            logger.debug("📊 query_metrics completed - Insights: {}", insights.size());

            String jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

            // Log the successful response
            McpLogger.logToolResponse("query_metrics", jsonResponse, true);

            return jsonResponse;

        } catch (Exception e) {
            logger.error("📊 Error in query_metrics", e);
            McpLogger.logError("query_metrics", e);
            try {
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("error", "Failed to query metrics: " + e.getMessage());
                errorResponse.put("expr", expr);
                errorResponse.put("range", range);
                String response = mapper.writeValueAsString(errorResponse);

                // Log the error response
                McpLogger.logToolResponse("query_metrics", response, false);
                return response;
            } catch (Exception jsonError) {
                String fallbackResponse = String.format("{\"error\":\"Failed to query metrics: %s\"}", e.getMessage());
                McpLogger.logToolResponse("query_metrics", fallbackResponse, false);
                return fallbackResponse;
            }
        }
    }

    @McpTool(
        name = "correlate_evidence",
        description = "Correlate findings from logs and metrics for the agentic AI triage system. Use this after gathering logs and metrics to identify patterns, timeline alignment, and root causes across the triage system services."
    )
    public String correlateEvidence(
            @Description("Description of log findings") String logFindings,
            @Description("Description of metric findings") String metricFindings
    ) {
        // Log the incoming MCP tool call
        McpLogger.logToolCall("correlate_evidence", Map.of(
            "logFindings", logFindings,
            "metricFindings", metricFindings
        ));

        logger.info("🔗 MCP Tool: correlate_evidence called");

        try {
            ObjectNode response = mapper.createObjectNode();
            response.put("logFindings", logFindings);
            response.put("metricFindings", metricFindings);

            // Build correlation analysis
            ObjectNode correlation = mapper.createObjectNode();
            correlation.put("timelineAlignment",
                "Analyze temporal alignment between error spikes and performance degradation");
            correlation.put("dependencyFailures",
                "Check if service dependency failures coincide with error increases");
            correlation.put("resourceExhaustion",
                "Correlate resource exhaustion patterns with error patterns");

            ObjectNode confidence = mapper.createObjectNode();
            confidence.put("level", "Medium");
            confidence.put("reasoning",
                "Confidence is HIGH if patterns align temporally, MEDIUM if partial alignment, LOW if no clear correlation");

            response.set("potentialCorrelations", correlation);
            response.set("confidence", confidence);

            String jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

            // Log the successful response
            McpLogger.logToolResponse("correlate_evidence", jsonResponse, true);

            return jsonResponse;

        } catch (Exception e) {
            logger.error("🔗 Error in correlate_evidence", e);
            McpLogger.logError("correlate_evidence", e);

            String fallbackResponse = String.format("{\"error\":\"Failed to correlate evidence: %s\"}", e.getMessage());
            McpLogger.logToolResponse("correlate_evidence", fallbackResponse, false);
            return fallbackResponse;
        }
    }

    @McpTool(
        name = "get_known_services",
        description = "Get the complete list of known services for accurate incident classification. Returns all available services organized by categories with domain mappings and usage instructions."
    )
    public String getKnownServices() {
        // Log the incoming MCP tool call (no parameters)
        McpLogger.logToolCall("get_known_services", Map.of());

        logger.info("🔧 MCP Tool: get_known_services called");

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("services.json");
            
            if (inputStream == null) {
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("error", "services.json configuration file not found");
                return mapper.writeValueAsString(errorResponse);
            }
            
            String servicesJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode config = mapper.readTree(servicesJson);
            
            // Build formatted response
            StringBuilder response = new StringBuilder();
            response.append("Known Services List:\n");
            
            JsonNode services = config.get("services");
            if (services != null && services.isArray()) {
                List<String> serviceList = new ArrayList<>();
                services.forEach(service -> serviceList.add(service.asText()));
                response.append(String.join(", ", serviceList));
            }
            
            response.append("\n\nService Categories:\n");
            JsonNode categories = config.get("categories");
            if (categories != null) {
                categories.fieldNames().forEachRemaining(categoryName -> {
                    JsonNode categoryServices = categories.get(categoryName);
                    if (categoryServices.isArray()) {
                        List<String> categoryList = new ArrayList<>();
                        categoryServices.forEach(service -> categoryList.add(service.asText()));
                        response.append(String.format("- %s: %s\n", categoryName, String.join(", ", categoryList)));
                    }
                });
            }
            
            response.append("\n");
            JsonNode instructions = config.get("usage_instructions");
            if (instructions != null) {
                response.append(instructions.asText());
            }
            
            logger.debug("🔧 get_known_services completed - {} services loaded",
                services != null ? services.size() : 0);

            String textResponse = response.toString();

            // Log the successful response
            McpLogger.logToolResponse("get_known_services", textResponse, true);

            return textResponse;

        } catch (Exception e) {
            logger.error("🔧 Error in get_known_services", e);
            McpLogger.logError("get_known_services", e);

            String errorResponse = String.format("Error loading services configuration: %s", e.getMessage());
            McpLogger.logToolResponse("get_known_services", errorResponse, false);
            return errorResponse;
        }
    }

    @McpResource(
        uriTemplate = "kb://runbooks/{serviceName}",
        name = "Service Runbook",
        description = "Get troubleshooting runbook for a specific service in the agentic AI triage system. Use this to access service-specific runbooks and troubleshooting guides.",
        mimeType = "text/markdown"
    )
    public String getRunbook(String serviceName) {
        // Log the incoming MCP resource access
        McpLogger.logResourceAccess("kb://runbooks/" + serviceName, "Service Runbook", serviceName);

        logger.info("📚 MCP Resource: getRunbook called - Service: {}", serviceName);

        try {
            String path = String.format("knowledge_base/%s-runbook.md", serviceName);
            InputStream in = getClass().getClassLoader().getResourceAsStream(path);

            if (in == null) {
                String notFoundResponse = String.format("# Runbook Not Found\n\nNo runbook available for service: %s", serviceName);
                McpLogger.logResourceResponse("kb://runbooks/" + serviceName, notFoundResponse.length(), false);
                return notFoundResponse;
            }

            String runbookContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            // Log the successful resource response
            McpLogger.logResourceResponse("kb://runbooks/" + serviceName, runbookContent.length(), true);

            return runbookContent;

        } catch (Exception e) {
            logger.error("📚 Error in getRunbook", e);
            McpLogger.logError("getRunbook", e);

            String errorResponse = String.format("# Error\n\nFailed to load runbook: %s", e.getMessage());
            McpLogger.logResourceResponse("kb://runbooks/" + serviceName, errorResponse.length(), false);
            return errorResponse;
        }
    }

    // ==================== HELPER METHODS ====================

    private record LogAnalysis(
        int errorCount,
        List<String> errorPatterns,
        Map<String, Integer> statusCodeCounts,
        List<String> anomalies,
        List<String> sampleErrorLines
    ) {}

    private LogAnalysis analyzeLogs(String logs) {
        if (logs == null || logs.isEmpty()) {
            return new LogAnalysis(0, List.of(), Map.of(), List.of(), List.of());
        }

        List<String> errorPatterns = new ArrayList<>();
        List<String> anomalies = new ArrayList<>();
        int errorCount = 0;
        Map<String, Integer> statusCounts = new HashMap<>();
        List<String> sampleErrorLines = new ArrayList<>();

        // Common error patterns
        Pattern errorPattern = Pattern.compile("(?i)(error|exception|failed|timeout|refused)");
        Pattern httpErrorPattern = Pattern.compile("(?i)(5\\d{2}|4\\d{2})");
        Pattern dbErrorPattern = Pattern.compile("(?i)(connection.*refused|deadlock|timeout.*database)");

        String[] lines = logs.split("\\n");
        for (String line : lines) {
            if (errorPattern.matcher(line).find()) {
                errorCount++;
                if (sampleErrorLines.size() < 5) {
                    sampleErrorLines.add(line.trim());
                }
            }

            Matcher httpMatcher = httpErrorPattern.matcher(line);
            if (httpMatcher.find()) {
                String code = httpMatcher.group(1);
                if (!errorPatterns.contains("HTTP " + code + " errors")) {
                    errorPatterns.add("HTTP " + code + " errors");
                }
                statusCounts.merge(code, 1, Integer::sum);
            }

            if (dbErrorPattern.matcher(line).find()) {
                if (!errorPatterns.contains("Database connectivity issues")) {
                    errorPatterns.add("Database connectivity issues");
                }
            }
        }

        // Detect anomalies
        if (errorCount > lines.length * 0.1) {
            anomalies.add(String.format("High error rate (%d errors in %d lines = %.1f%%)",
                errorCount, lines.length, (errorCount * 100.0 / lines.length)));
        }

        return new LogAnalysis(errorCount, errorPatterns, statusCounts, anomalies, sampleErrorLines);
    }

    private String determineMetricsFile(String expr) {
        String e = expr == null ? "" : expr.toLowerCase();

        // Service-specific routing
        if (e.contains("gateway") && (e.contains("error") || e.contains("5xx"))) {
            return "metrics/api-gateway-errors.json";
        }
        if (e.contains("checkout") && (e.contains("latency") || e.contains("p95") || e.contains("response_time"))) {
            return "metrics/checkout-service-latency.json";
        }
        if (e.contains("auth") && (e.contains("error") || e.contains("fail"))) {
            return "metrics/auth-service-errors.json";
        }
        if (e.contains("db") || e.contains("database")) {
            return "metrics/db-performance.json";
        }

        // Generic mappings
        if (e.contains("error") || e.contains("fail")) {
            return "metrics/payment-service-errors.json";
        } else if (e.contains("latency") || e.contains("response_time")) {
            return "metrics/payment-service-latency.json";
        } else if (e.contains("cpu") || e.contains("memory") || e.contains("resource")) {
            return "metrics/user-service-resources.json";
        } else if (e.contains("throughput") || e.contains("rate")) {
            return "metrics/order-service-throughput.json";
        } else {
            return "metrics/payment-service-errors.json";
        }
    }

    private String formatMetricsOutput(JsonNode metricsData, String expr, String range) {
        StringBuilder output = new StringBuilder();
        output.append("Metrics Data Summary:\n");

        JsonNode metrics = metricsData.get("metrics");
        if (metrics == null) {
            return "Invalid metrics file format";
        }

        // Format error metrics
        if (metrics.has("error_rate")) {
            JsonNode errorRate = metrics.get("error_rate");
            output.append(String.format("- Error Rate: %.1f%% (%s), Previous: %.1f%%\n",
                errorRate.get("current").asDouble(),
                errorRate.get("status").asText(),
                errorRate.get("previous_hour").asDouble()));

            JsonNode errorCount = metrics.get("error_count");
            output.append(String.format("- Total Errors: %d requests\n",
                errorCount.get("total").asInt()));

            if (metrics.has("error_spike") && metrics.get("error_spike").get("detected").asBoolean()) {
                JsonNode spike = metrics.get("error_spike");
                output.append(String.format("- Spike Detected: %s (peak: %.1f%%, cause: %s)\n",
                    spike.get("time_window").asText(),
                    spike.get("peak_rate").asDouble(),
                    spike.get("primary_cause").asText()));
            }
        }

        // Format latency metrics
        if (metrics.has("latency_percentiles")) {
            JsonNode latency = metrics.get("latency_percentiles");
            output.append(String.format("- Latency P95: %dms, P99: %dms, P99.9: %dms\n",
                latency.get("p95").asInt(),
                latency.get("p99").asInt(),
                latency.get("p99.9").asInt()));

            JsonNode avgLatency = metrics.get("average_latency");
            output.append(String.format("- Average Latency: %dms (%s), Baseline: %dms\n",
                avgLatency.get("current").asInt(),
                avgLatency.get("status").asText(),
                avgLatency.get("baseline").asInt()));
        }

        // Format resource metrics
        if (metrics.has("cpu_utilization")) {
            JsonNode cpu = metrics.get("cpu_utilization");
            output.append(String.format("- CPU Usage: %.1f%% (%s), Peak: %.1f%%\n",
                cpu.get("current").asDouble(),
                cpu.get("status").asText(),
                cpu.get("peak_15min").asDouble()));

            JsonNode memory = metrics.get("memory_utilization");
            output.append(String.format("- Memory: Heap %.1f%%, GC Pressure: %s\n",
                memory.get("heap_used").asDouble(),
                memory.get("gc_pressure").asText()));
        }

        // Format throughput metrics
        if (metrics.has("request_rate")) {
            JsonNode requestRate = metrics.get("request_rate");
            output.append(String.format("- Request Rate: %d req/sec, Peak: %d req/sec\n",
                requestRate.get("current").asInt(),
                requestRate.get("peak_1h").asInt()));

            JsonNode successRate = metrics.get("success_rate");
            output.append(String.format("- Success Rate: %.1f%% (%s), Target: %.1f%%\n",
                successRate.get("current").asDouble(),
                successRate.get("status").asText(),
                successRate.get("target").asDouble()));
        }

        // Add alerts
        if (metrics.has("alerts")) {
            JsonNode alerts = metrics.get("alerts");
            if (alerts.isArray() && alerts.size() > 0) {
                output.append("- Active Alerts: ");
                for (JsonNode alert : alerts) {
                    output.append(alert.asText()).append("; ");
                }
                output.append("\n");
            }
        }

        return output.toString();
    }

    private List<String> analyzeMetrics(String metrics, String expr) {
        List<String> insights = new ArrayList<>();

        if (metrics == null || metrics.isEmpty()) {
            insights.add("No metrics data available");
            return insights;
        }

        // Simple heuristic analysis
        if (expr.contains("error")) {
            insights.add("Error rate metrics requested - indicates error investigation");
        }
        if (expr.contains("latency") || expr.contains("response_time")) {
            insights.add("Performance metrics requested - indicates latency investigation");
        }
        if (expr.contains("cpu") || expr.contains("memory")) {
            insights.add("Resource utilization metrics - indicates capacity investigation");
        }

        // Look for extreme values
        if (metrics.contains("100%") || metrics.contains("0.00")) {
            insights.add("Extreme values detected - potential system limits or failures");
        }

        return insights;
    }
}
