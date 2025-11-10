#!/bin/bash

# ============================================================================
# MCP (Model Context Protocol) - Complete cURL Examples
# ============================================================================
#
# This file demonstrates the complete MCP protocol flow using curl commands.
# The MCP protocol uses JSON-RPC 2.0 over HTTP.
#
# Service: evidence-tools
# Endpoint: http://localhost:9200/mcp
# Protocol: JSON-RPC 2.0
#
# USAGE:
#   1. Start the service: mvn exec:java
#   2. Run individual commands below, or source this file
#   3. Each command is self-contained and can be run independently
#
# ============================================================================

# Base URL for the MCP endpoint
MCP_URL="http://localhost:9200/mcp"

echo "======================================"
echo "MCP Protocol - cURL Examples"
echo "======================================"
echo ""

# ============================================================================
# 1. INITIALIZE - Establish connection with the MCP server
# ============================================================================
echo "1. INITIALIZE - Establish MCP session"
echo "--------------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ============================================================================
# 2. LIST TOOLS - Discover all available tools
# ============================================================================
echo "2. LIST TOOLS - Discover available tools"
echo "-----------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }' | jq '.'

echo -e "\n\n"

# ============================================================================
# 3. LIST RESOURCES - Discover available resources
# ============================================================================
echo "3. LIST RESOURCES - Discover available resources"
echo "-------------------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "resources/list",
    "params": {}
  }' | jq '.'

echo -e "\n\n"

# ============================================================================
# 4. TOOL CALLS - Invoke specific tools
# ============================================================================

echo "======================================"
echo "TOOL CALLS"
echo "======================================"
echo ""

# ----------------------------------------------------------------------------
# 4.1. LOG TOOLS - fetch_logs
# ----------------------------------------------------------------------------
echo "4.1. TOOL: fetch_logs (Payment Service)"
echo "----------------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 4.2. LOG TOOLS - fetch_logs (Checkout Service)
# ----------------------------------------------------------------------------
echo "4.2. TOOL: fetch_logs (Checkout Service)"
echo "-----------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "fetch_logs",
      "arguments": {
        "service": "checkout-service",
        "lines": 15
      }
    }
  }' | jq '.'

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 4.3. METRICS TOOLS - query_metrics (Error Rate)
# ----------------------------------------------------------------------------
echo "4.3. TOOL: query_metrics (Error Rate)"
echo "--------------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 4.4. METRICS TOOLS - query_metrics (Latency)
# ----------------------------------------------------------------------------
echo "4.4. TOOL: query_metrics (Latency)"
echo "-----------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 4.5. METRICS TOOLS - query_metrics (CPU Usage)
# ----------------------------------------------------------------------------
echo "4.5. TOOL: query_metrics (CPU Usage)"
echo "-------------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 4.6. KNOWLEDGE BASE TOOLS - get_known_services
# ----------------------------------------------------------------------------
echo "4.6. TOOL: get_known_services"
echo "------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 4.7. ANALYSIS TOOLS - correlate_evidence
# ----------------------------------------------------------------------------
echo "4.7. TOOL: correlate_evidence"
echo "------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

# ============================================================================
# 5. RESOURCE ACCESS - Read resources
# ============================================================================

echo "======================================"
echo "RESOURCE ACCESS"
echo "======================================"
echo ""

# ----------------------------------------------------------------------------
# 5.1. Read Payment Service Runbook
# ----------------------------------------------------------------------------
echo "5.1. RESOURCE: Payment Service Runbook"
echo "---------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 11,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/payment-service"
    }
  }' | jq '.'

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 5.2. Read Checkout Service Runbook
# ----------------------------------------------------------------------------
echo "5.2. RESOURCE: Checkout Service Runbook"
echo "----------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 12,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/checkout-service"
    }
  }' | jq '.'

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 5.3. Read Auth Service Runbook
# ----------------------------------------------------------------------------
echo "5.3. RESOURCE: Auth Service Runbook"
echo "------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 13,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/auth-service"
    }
  }' | jq '.'

echo -e "\n\n"

# ============================================================================
# 6. ERROR HANDLING EXAMPLES
# ============================================================================

echo "======================================"
echo "ERROR HANDLING EXAMPLES"
echo "======================================"
echo ""

# ----------------------------------------------------------------------------
# 6.1. Invalid Tool Name
# ----------------------------------------------------------------------------
echo "6.1. ERROR: Invalid Tool Name"
echo "------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 14,
    "method": "tools/call",
    "params": {
      "name": "invalid_tool",
      "arguments": {}
    }
  }' | jq '.'

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 6.2. Non-existent Service Logs
# ----------------------------------------------------------------------------
echo "6.2. ERROR: Non-existent Service Logs"
echo "--------------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 15,
    "method": "tools/call",
    "params": {
      "name": "fetch_logs",
      "arguments": {
        "service": "non-existent-service",
        "lines": 10
      }
    }
  }' | jq '.'

echo -e "\n\n"

# ----------------------------------------------------------------------------
# 6.3. Non-existent Runbook
# ----------------------------------------------------------------------------
echo "6.3. ERROR: Non-existent Runbook"
echo "---------------------------------"

curl -X POST "$MCP_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 16,
    "method": "resources/read",
    "params": {
      "uri": "kb://runbooks/non-existent-service"
    }
  }' | jq '.'

echo -e "\n\n"

# ============================================================================
# 7. BATCH REQUESTS - Multiple operations in one call
# ============================================================================

echo "======================================"
echo "BATCH REQUEST EXAMPLE"
echo "======================================"
echo ""

echo "7. BATCH: Get services + Fetch logs + Query metrics"
echo "----------------------------------------------------"

curl -X POST "$MCP_URL" \
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

echo -e "\n\n"

echo "======================================"
echo "All examples completed!"
echo "======================================"
echo ""
echo "TIP: You can run individual requests by copying the curl command"
echo "TIP: Add '| jq' to any curl command for pretty JSON formatting"
echo "TIP: Remove '| jq' if you don't have jq installed"
echo ""
