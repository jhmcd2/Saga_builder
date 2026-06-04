# NameServer Resources Export API - Quick Reference Guide

## Quick Start

### 1. Access the API

```bash
# Base URL
http://localhost:8080/resources-export

# List available domains
curl http://localhost:8080/resources-export/
```

### 2. Available Endpoints

| Endpoint | Purpose | Query Params |
|----------|---------|--------------|
| `/` | List domains and endpoints | None |
| `/resources` | Export all resource routes | `domain` (optional) |
| `/netResources` | Export local net→resources | `domain` (optional) |
| `/nameServerResources` | Export remote nameserver resources | `domain` (optional) |
| `/all` | Export everything | `domain` (optional) |

### 3. Quick Examples

```bash
# Get all resources
curl http://localhost:8080/resources-export/resources

# Get specific domain
curl "http://localhost:8080/resources-export/resources?domain=example.com"

# Get everything (pretty printed)
curl -s http://localhost:8080/resources-export/all | jq .

# Check response status
curl -i http://localhost:8080/resources-export/
```

## Swagger Setup (Quick Path)

### Fastest Way: Use Online Swagger Editor

1. Go to: https://editor.swagger.io/
2. Paste the OpenAPI spec provided in the full guide
3. Set server URL to: `http://localhost:8080`
4. Click "Try it out" on any endpoint

### Local Swagger UI

```bash
# Option 1: Docker
docker run -p 8080:8080 \
  -e SWAGGER_URL=http://localhost:8080/v3/api-docs \
  swaggerapi/swagger-ui

# Option 2: Add to project (see full guide)
# Then access: http://localhost:8080/swagger-ui/
```

## Response Structure

### Success Response (200)

```json
{
  "resources": { /* data */ },
  "count": 123,
  "timestamp": 1715668512345
}
```

### Error Response (404 or 500)

```json
{
  "error": "Error message here",
  "status": 404,
  "timestamp": 1715668512345
}
```

## Common Tasks

### Task: Export all resources from a domain

```bash
curl "http://localhost:8080/resources-export/resources?domain=myserver"
```

### Task: Monitor resource updates

```bash
# Run every 10 seconds
watch -n 10 'curl -s http://localhost:8080/resources-export/all | jq .exportTimestamp'
```

### Task: Count resources

```bash
curl -s http://localhost:8080/resources-export/resources | jq '.count'
```

### Task: List all resource IDs

```bash
curl -s http://localhost:8080/resources-export/resources | \
  jq 'keys_unsorted'
```

### Task: Find a specific resource

```bash
curl -s http://localhost:8080/resources-export/resources | \
  jq '.resources | to_entries[] | select(.key | contains("search-term"))'
```

## Files Changed Summary

```
Added 5 files:
  + core/silkwave.nameserver/.../dto/NetIdDTO.java
  + core/silkwave.nameserver/.../dto/ResourceRouteDTO.java
  + core/silkwave.nameserver/.../dto/NetResourcesDTO.java
  + core/silkwave.nameserver/.../dto/ResourcesExportDTO.java
  + core/core/.../web/ResourcesExportServlet.java

Modified 2 files:
  ~ core/silkwave.nameserver/.../NameServer.java (+3 methods)
  ~ core/core/.../web/WebServer.java (+1 line)
```

## Troubleshooting Quick Tips

| Problem | Solution |
|---------|----------|
| Connection refused | Start application: `gradle run` |
| 404 error | Check URL path and domain parameter |
| Empty results | Register resources first via main API |
| Swagger not loading | Use online editor: https://editor.swagger.io/ |
| Slow response | Try specific domain instead of all |

## Performance Tips

✓ Use specific domain parameter  
✓ Query specific endpoint instead of `/all`  
✓ Enable client-side caching  
✓ Don't poll too frequently  
✓ Monitor resource growth  

## Thread Safety

✓ All endpoints are thread-safe  
✓ Uses read locks from existing NameServer  
✓ Safe for concurrent requests  
✓ No data corruption risk  

## API Documentation

For complete details, see: `API_INTEGRATION_GUIDE.md`

---

**Version**: 1.0  
**Status**: Ready for Production  
**Branch**: api_topology_export
