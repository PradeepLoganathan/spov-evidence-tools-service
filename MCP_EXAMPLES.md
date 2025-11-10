# MCP Protocol Examples - Evidence Tools

This guide demonstrates how to interact with the Evidence Tools MCP service using curl commands.

## Prerequisites

1. **Start the service:**
   ```bash
   mvn exec:java
   ```

2. **Service endpoint:**
   ```
   http://localhost:9200/mcp
   ```

3. **Optional: Install jq for pretty JSON formatting**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install jq

   # macOS
   brew install jq
   ```

---

## Quick Start

### Run All Examples
```bash
./MCP_CURL_EXAMPLES.sh
```

### Run Individual Commands
Copy and paste any curl command below.

---

## MCP Protocol Flow

### 1. Initialize Connection

Establish a session with the MCP server.

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {
        "roots": {
          "listChanged": true
        }
      },
      "clientInfo": {
        "name": "curl-client",
        "version": "1.0.0"
      }
    }
  }' | jq '.'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {},
      "resources": {}
    },
    "serverInfo": {
      "name": "evidence-tools",
      "version": "1.0.0"
    }
  }
}
```

---

### 2. Discover Available Tools

List all tools provided by the service.

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }' | jq '.'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "tools": [
      {
        "name": "fetch_logs",
        "description": "Fetch logs from the agentic AI triage system services...",
        "inputSchema": {
          "type": "object",
          "properties": {
            "service": { "type": "string" },
            "lines": { "type": "integer" }
          }
        }
      },
      {
        "name": "query_metrics",
        "description": "Query performance metrics...",
        ...
      },
      {
        "name": "get_known_services",
        ...
      },
      {
        "name": "correlate_evidence",
        ...
      }
    ]
  }
}
```

---

### 3. Discover Available Resources

List all resources (like runbooks) provided by the service.

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "resources/list",
    "params": {}
  }' | jq '.'
```

---

## Tool Examples

### 📝 LOG TOOLS

#### Fetch Payment Service Logs

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "fetch_logs",
      "arguments": {
        "service": "payment-service",
        "lines": 10
      }
    }
  }' | jq '.'
```

**Response includes:**
- Raw log lines
- Error count and patterns
- HTTP status code analysis
- Anomaly detection
- Sample error lines

#### Fetch Checkout Service Logs

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "fetch_logs",
      "arguments": {
        "service": "checkout-service",
        "lines": 20
      }
    }
  }' | jq '.'
```

---

### 📊 METRICS TOOLS

#### Query Error Rate Metrics

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 6,
    "method": "tools/call",
    "params": {
      "name": "query_metrics",
      "arguments": {
        "expr": "error_rate",
        "range": "1h"
      }
    }
  }' | jq '.'
```

**Response includes:**
- Raw metrics data (JSON)
- Formatted summary
- Error spike detection
- Insights

#### Query Latency Metrics

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 7,
    "method": "tools/call",
    "params": {
      "name": "query_metrics",
      "arguments": {
        "expr": "latency",
        "range": "30m"
      }
    }
  }' | jq '.'
```

**Response includes:**
- P95, P99, P99.9 latencies
- Average latency vs baseline
- Performance trends

#### Query Resource Metrics

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 8,
    "method": "tools/call",
    "params": {
      "name": "query_metrics",
      "arguments": {
        "expr": "cpu_usage",
        "range": "15m"
      }
    }
  }' | jq '.'
```

---

### 📚 KNOWLEDGE BASE TOOLS

#### Get Known Services

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 9,
    "method": "tools/call",
    "params": {
      "name": "get_known_services",
      "arguments": {}
    }
  }' | jq '.'
```

**Response includes:**
- Complete service list
- Service categories (Core Business, Infrastructure, etc.)
- Usage instructions

---

### 🔗 ANALYSIS TOOLS

#### Correlate Evidence

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 10,
    "method": "tools/call",
    "params": {
      "name": "correlate_evidence",
      "arguments": {
        "logFindings": "High rate of HTTP 503 errors and database timeouts observed at 14:30 UTC",
        "metricFindings": "CPU spike to 95% at 14:28 UTC, database connection pool at 98% capacity"
      }
    }
  }' | jq '.'
```

**Response includes:**
- Timeline alignment analysis
- Dependency failure correlation
- Resource exhaustion patterns
- Confidence assessment

---

## Resource Examples

### Read Service Runbooks

#### Payment Service Runbook

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 11,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/payment-service"
    }
  }' | jq '.'
```

**Response includes:**
- Markdown-formatted runbook
- Symptoms and quick checks
- Known patterns
- Playbook steps
- Rollback procedures

#### Checkout Service Runbook

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 12,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/checkout-service"
    }
  }' | jq '.'
```

#### Auth Service Runbook

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 13,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/auth-service"
    }
  }' | jq '.'
```

#### API Gateway Runbook

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 14,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/api-gateway"
    }
  }' | jq '.'
```

---

## Error Handling Examples

### Invalid Tool Name

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 15,
    "method": "tools/call",
    "params": {
      "name": "invalid_tool",
      "arguments": {}
    }
  }' | jq '.'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 15,
  "error": {
    "code": -32601,
    "message": "Tool not found: invalid_tool"
  }
}
```

### Non-existent Service

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 16,
    "method": "tools/call",
    "params": {
      "name": "fetch_logs",
      "arguments": {
        "service": "non-existent-service",
        "lines": 10
      }
    }
  }' | jq '.'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 16,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\"error\":\"No log file found for service: non-existent-service\",\"service\":\"non-existent-service\"}"
      }
    ]
  }
}
```

---

## Batch Requests

Send multiple requests in a single HTTP call:

```bash
curl -X POST "http://localhost:9200/mcp" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "jsonrpc": "2.0",
      "id": 17,
      "method": "tools/call",
      "params": {
        "name": "get_known_services",
        "arguments": {}
      }
    },
    {
      "jsonrpc": "2.0",
      "id": 18,
      "method": "tools/call",
      "params": {
        "name": "fetch_logs",
        "arguments": {
          "service": "payment-service",
          "lines": 5
        }
      }
    },
    {
      "jsonrpc": "2.0",
      "id": 19,
      "method": "tools/call",
      "params": {
        "name": "query_metrics",
        "arguments": {
          "expr": "error_rate",
          "range": "1h"
        }
      }
    }
  ]' | jq '.'
```

---

## JSON-RPC Response Format

All responses follow the JSON-RPC 2.0 format:

### Successful Response
```json
{
  "jsonrpc": "2.0",
  "id": <request-id>,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "<tool-response-data>"
      }
    ]
  }
}
```

### Error Response
```json
{
  "jsonrpc": "2.0",
  "id": <request-id>,
  "error": {
    "code": <error-code>,
    "message": "<error-message>"
  }
}
```

---

## Available Services

The following services have logs, metrics, and runbooks available:

- `payment-service`
- `checkout-service`
- `auth-service`
- `api-gateway`
- `order-service`
- `user-service`

---

## Tips

1. **Use jq for formatting**: Add `| jq '.'` to any curl command for pretty JSON output
2. **Remove jq if not installed**: Just remove `| jq '.'` from the command
3. **Check service is running**: Make sure `mvn exec:java` is running before executing commands
4. **Use the shell script**: Run `./MCP_CURL_EXAMPLES.sh` to execute all examples at once
5. **Batch requests**: Send multiple requests in an array to reduce HTTP round trips

---

## Further Reading

- [MCP Protocol Specification](https://spec.modelcontextprotocol.io/)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)
- See `ARCHITECTURE.md` for detailed architecture documentation
