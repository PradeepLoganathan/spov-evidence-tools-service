# AgenticAI Triage MCP Tools

Standalone MCP (Model Context Protocol) service that provides evidence gathering tools for incident triage and analysis. This provides the necessary tools for the agentic triage workflow system.

## Purpose

This service exposes MCP tools that can be consumed by:
- **Akka Agents** in other services (via `RemoteMcpTools.fromService("evidence-tools")`)
- **External MCP clients** (Claude Desktop, VSCode extensions, etc.)
- **Direct HTTP calls** via JSON-RPC 2.0

## Architecture

```
┌─────────────────────────────────────┐
│  Triage Service (port 9100)         │
│                                     │
│  EvidenceAgent uses:                │
│  RemoteMcpTools.fromService(        │
│    "evidence-tools"                 │
│  )                                  │
└──────────────┬──────────────────────┘
               │
               │ Service Discovery
               │ (dev-mode)
               ↓
┌─────────────────────────────────────┐
│  Evidence Tools Service (port 9200) │
│                                     │
│  @McpEndpoint                       │
│  - fetch_logs                       │
│  - query_metrics                    │
│  - correlate_evidence               │
└─────────────────────────────────────┘
```

## Configuration

**Service Name:** `evidence-tools` (must match in both services)
**Port:** `9200`
**MCP Endpoint:** `http://localhost:9200/mcp`

## Running the Service

### Start Evidence Tools Service (Terminal 1)
```bash
cd agenticai-triage-mcp-tools
mvn compile exec:java
```

Look for:
```
INFO  akka.runtime.DiscoveryManager - Akka Runtime started at 127.0.0.1:9200
INFO  ... - MCP endpoint component [...EvidenceToolsEndpoint], path [/mcp]
INFO  c.e.evidence.EvidenceToolsEndpoint - 📊 MCP Tool: query_metrics called - Expr: errors:rate1m, Range: 30m
INFO  c.e.evidence.EvidenceToolsEndpoint - 📝 MCP Tool: fetch_logs called - Service: payment-service, Lines: 200
INFO  c.e.evidence.EvidenceToolsEndpoint - 🔗 MCP Tool: correlate_evidence called
```

### Start Triage Service (Terminal 2)
```bash
cd spov-sample-agentic-workflow
export OPENAI_API_KEY="your-key-here"
mvn compile exec:java
```

Look for:
```
INFO  akka.runtime.DiscoveryManager - Akka Runtime started at 127.0.0.1:9100
```

## Testing

### Test MCP Endpoint Directly
```bash
curl -s http://localhost:9200/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"tools/list","params":{}}' \
  | python3 -m json.tool
```

### Test from Triage Workflow
```bash
curl -X POST http://localhost:9100/triage/test-123 \
  -H "Content-Type: application/json" \
  -d '{
    "incident": "Payment service is experiencing high error rates"
  }'

# Check status
curl http://localhost:9100/triage/test-123
```

## MCP Tools Provided

### 1. fetch_logs
Fetch service logs with automatic error analysis

**Arguments:**
- `service` (string) - Service name (e.g., "payment-service")
- `lines` (integer) - Number of log lines to fetch

**Returns:** Logs with error count, patterns, anomalies, and sample errors

### 2. query_metrics
Query performance metrics with insights

**Arguments:**
- `expr` (string) - Metrics expression (e.g., "error_rate", "latency")
- `range` (string) - Time range (e.g., "1h", "30m")

**Returns:** Parsed metrics with formatted summary and insights

### 3. correlate_evidence
Correlate findings across logs and metrics

**Arguments:**
- `logFindings` (string) - Description of log findings
- `metricFindings` (string) - Description of metric findings

**Returns:** Correlation analysis with confidence assessment

## Service Discovery (Dev Mode)

In development mode, services discover each other via:

1. **Service Name**: Set in `application.conf`:
   ```hocon
   akka.javasdk.dev-mode.service-name = "evidence-tools"
   ```

2. **Port**: Each service runs on a unique port:
   - Evidence Tools: `9200`
   - Triage Service: `9100`

3. **Agent Connection**: Agents in other services reference this service:
   ```java
   RemoteMcpTools.fromService("evidence-tools")
   ```

Akka automatically discovers the service running locally and routes MCP tool calls to it.

## Production Deployment

For production, services would be deployed separately and discover each other via:
- Kubernetes service discovery
- Consul/Eureka
- DNS-based discovery
- Configured endpoints

## Files

```
agenticai-triage-mcp-tools/
├── pom.xml                           # Maven configuration
├── README.md                         # This file
├── src/main/
│   ├── java/com/pradeepl/evidence/
│   │   └── EvidenceToolsEndpoint.java  # MCP endpoint with tools
│   └── resources/
│       ├── application.conf          # Service configuration
│       ├── logs/                     # Sample log files
│       ├── metrics/                  # Sample metrics files
│       └── knowledge_base/           # Runbooks and incident reports
```

## Troubleshooting


### Service Not Found
Ensure:
1. Evidence Tools service is running on port 9200
2. `service-name` matches in both services
3. Both services are in dev-mode (not production)

### MCP Tools Not Working
Check logs for:
```
Building component [com.example.evidence.EvidenceToolsEndpoint]
MCP endpoint component [...], path [/mcp]
```
