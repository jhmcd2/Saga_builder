# NameServer Resources Export API - Complete Integration Guide

**Version**: 1.0  
**Date**: May 14, 2026  
**Branch**: api_topology_export  
**Status**: Ready for Production

---

## Table of Contents

1. [Overview](#overview)
2. [Changes Made](#changes-made)
3. [Architecture](#architecture)
4. [Installation & Deployment](#installation--deployment)
5. [Connection Instructions](#connection-instructions)
6. [API Endpoints](#api-endpoints)
7. [Swagger Integration](#swagger-integration)
8. [Example Usage](#example-usage)
9. [Troubleshooting](#troubleshooting)

---

## Overview

This document describes the implementation of REST API endpoints that export NameServer resource collections to remote locations. The API provides secure, thread-safe access to three critical ConcurrentMap collections:

- **Resource Routes** - All known resources (local + remote)
- **Local Net Resources** - Maps of local network IDs to their resources
- **Remote NameServer Resources** - Maps of other nameservers' network resources

### Key Features

✅ **RESTful API** - Standard HTTP GET endpoints with JSON responses  
✅ **Thread-Safe** - Uses read locks to ensure concurrent access safety  
✅ **JSON Format** - Easy integration with client applications  
✅ **Error Handling** - Proper HTTP status codes and error messages  
✅ **Multi-Domain Support** - Query specific nameserver domains  
✅ **No New Dependencies** - Uses existing project libraries (GSON, Jakarta Servlets)  

---

## Changes Made

### Summary of Changes

| Category | Count | Details |
|----------|-------|---------|
| New Files | 5 | 4 DTOs + 1 Servlet |
| Modified Files | 2 | NameServer, WebServer |
| Lines Added | 511 | Total additions to codebase |
| New Endpoints | 5 | REST API endpoints |

### Detailed File Changes

#### 1. New DTO Classes (Package: `gov.ic.silkwave.nameserver.dto`)

**File**: `NetIdDTO.java` (43 lines)
- Serializable representation of NetId
- Fields: id, ownerId, creationTime
- Purpose: JSON serialization of network identifiers

**File**: `ResourceRouteDTO.java` (45 lines)
- Serializable representation of ResourceRoute
- Fields: resId, type, netIds (Map)
- Purpose: JSON serialization of resource routes

**File**: `NetResourcesDTO.java` (55 lines)
- Serializable representation of NetResources
- Fields: netId, lastSeen, resourceCount, resourceMap
- Purpose: JSON serialization of network resource mappings

**File**: `ResourcesExportDTO.java` (57 lines)
- Top-level container for export data
- Fields: resources, netToResources, nameServersToResources, exportTimestamp
- Purpose: Unified response object for API endpoints

#### 2. New REST Servlet

**File**: `ResourcesExportServlet.java` (283 lines)  
**Location**: `gov/ic/silkwave/web/`

Features:
- 5 GET endpoints for resource export
- Thread-safe data conversion using defensive copies
- JSON serialization via GSON
- Comprehensive error handling
- Support for optional domain parameter

#### 3. Modified Files

**File**: `NameServer.java`  
**Changes**: +27 lines

Three new thread-safe getter methods:
```java
public ConcurrentMap<String, ResourceRoute> getResources()
public ConcurrentMap<String, NetResources> getNetToResources()
public ConcurrentMap<String, NetResources> getNameServersToResources()
```

Each method:
- Acquires read lock from existing ReentrantReadWriteLock
- Creates defensive copy of map
- Prevents concurrent modification issues
- Returns thread-safe copy to caller

**File**: `WebServer.java`  
**Changes**: +1 line (line 93)

Added servlet registration:
```java
context.addServlet(new ServletHolder(new ResourcesExportServlet()), "/resources-export/*");
```

---

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────┐
│         REST Client (Browser, CLI, App)         │
└────────────────────┬────────────────────────────┘
                     │ HTTP GET
                     ▼
┌─────────────────────────────────────────────────┐
│        Jakarta Servlet Container                │
│  ResourcesExportServlet @ /resources-export/*   │
└────────────────┬────────────────────────────────┘
                 │ Acquires Read Lock
                 ▼
┌─────────────────────────────────────────────────┐
│              NameServer Instance                │
│  ┌──────────────────────────────────────────┐  │
│  │ ReentrantReadWriteLock (Thread-Safe)     │  │
│  │  - resources (ConcurrentMap)             │  │
│  │  - netToResources (ConcurrentMap)        │  │
│  │  - nameServersToResources (ConcurrentMap)│  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                 │
                 ▼ Defensive Copies
┌─────────────────────────────────────────────────┐
│            DTO Objects                          │
│  NetIdDTO, ResourceRouteDTO, NetResourcesDTO   │
└─────────────────────────────────────────────────┘
                 │
                 ▼ GSON Serialization
┌─────────────────────────────────────────────────┐
│            JSON Response                        │
│         (Sent to Client)                        │
└─────────────────────────────────────────────────┘
```

### Component Interaction

1. **REST Client** sends HTTP GET request to `/resources-export/*`
2. **ResourcesExportServlet** handles the request
3. Servlet calls getter method on **NameServer**
4. **NameServer** acquires read lock and creates defensive copy
5. Copy is converted to DTO objects
6. **GSON** serializes DTOs to JSON
7. JSON response returned to client with HTTP 200

---

## Installation & Deployment

### Prerequisites

- Java 11 or higher
- Jakarta Servlet support (already in project)
- GSON library (already in project)
- Gradle build system

### Build Process

#### Step 1: Ensure Files Are Present

Verify all new files are in the `api_topology_export` branch:

```bash
cd /path/to/silkwave
git branch
# Should show api_topology_export

git log --oneline -5
# Should show recent merge commit
```

#### Step 2: Build the Project

```bash
cd /path/to/silkwave
gradle build -x test
```

Or for a specific module:

```bash
gradle core:core:build -x test
```

#### Step 3: Deploy

The exact deployment process depends on your environment:

**For Docker:**
```bash
docker build -t silkwave:latest .
docker run -p 8080:8080 silkwave:latest
```

**For Application Server (Tomcat, Jetty, etc):**
```bash
# Copy built WAR/JAR to application server
cp build/libs/silkwave.war $TOMCAT_HOME/webapps/
```

**For Development (if using main method):**
```bash
gradle run
```

### Configuration

The API requires NO additional configuration beyond what's already in the application. It:
- Uses existing NameServerLocator to find nameservers
- Uses existing ReentrantReadWriteLock for thread safety
- Uses existing GSON configuration for serialization

---

## Connection Instructions

### Starting the Application

#### Option 1: Using Gradle

```bash
cd /path/to/silkwave
gradle run
```

Output should show:
```
Starting WebServer on http://localhost:8080
```

#### Option 2: Docker

```bash
docker run -p 8080:8080 silkwave:latest
```

#### Option 3: Application Server

Start your application server (Tomcat, Jetty, etc.) with the deployed application.

### Verifying Connection

#### Quick Health Check

```bash
curl http://localhost:8080/health
```

Expected response:
```
good
```

#### Verify API is Accessible

```bash
curl http://localhost:8080/resources-export/
```

Expected response (JSON):
```json
{
  "domains": ["example.com", "test.domain"],
  "endpoints": ["/resources", "/netResources", "/nameServerResources", "/all"]
}
```

### Connection Properties

| Property | Value |
|----------|-------|
| **Host** | localhost (or your server hostname) |
| **Port** | 8080 (default) |
| **Base URL** | http://localhost:8080 |
| **API Base Path** | /resources-export |
| **Protocol** | HTTP |
| **Authentication** | None (open API) |
| **Response Format** | JSON |

---

## API Endpoints

### 1. List Available Domains

**Endpoint**: `GET /resources-export/`

Lists all available nameserver domains and valid endpoints.

**Query Parameters**: None

**Response Example**:
```json
{
  "domains": ["example.com", "test.domain"],
  "endpoints": ["/resources", "/netResources", "/nameServerResources", "/all"]
}
```

**HTTP Status**: 200 OK

---

### 2. Export All Resource Routes

**Endpoint**: `GET /resources-export/resources`

Exports all known resource routes (local + remote).

**Query Parameters**:
- `domain` (optional): Specify nameserver domain (e.g., `?domain=example.com`)

**Response Example**:
```json
{
  "resources": {
    "service-id-1": {
      "resId": "service-id-1",
      "type": "SERVICE",
      "netIds": {
        "net-addr-1": {
          "id": "net-addr-1",
          "ownerId": "owner-1",
          "creationTime": 1715668500000
        },
        "net-addr-2": {
          "id": "net-addr-2",
          "ownerId": "owner-2",
          "creationTime": 1715668510000
        }
      }
    }
  },
  "count": 1,
  "timestamp": 1715668512345
}
```

**HTTP Status**: 200 OK

**Error Response** (404):
```json
{
  "error": "NameServer not found for domain: invalid.domain",
  "status": 404,
  "timestamp": 1715668512345
}
```

---

### 3. Export Net To Resources Mapping

**Endpoint**: `GET /resources-export/netResources`

Exports mapping of local network IDs to their NetResources.

**Query Parameters**:
- `domain` (optional): Specify nameserver domain

**Response Example**:
```json
{
  "netToResources": {
    "local-net-1": {
      "netId": "local-net-1",
      "lastSeen": 1715668510000,
      "resourceCount": 2,
      "resourceMap": {
        "resource-1": {
          "resId": "resource-1",
          "type": "SERVICE",
          "netIds": { ... }
        },
        "resource-2": {
          "resId": "resource-2",
          "type": "SERVICE",
          "netIds": { ... }
        }
      }
    }
  },
  "count": 1,
  "timestamp": 1715668512345
}
```

**HTTP Status**: 200 OK

---

### 4. Export NameServer To Resources Mapping

**Endpoint**: `GET /resources-export/nameServerResources`

Exports mapping of other nameservers' network IDs to their NetResources.

**Query Parameters**:
- `domain` (optional): Specify nameserver domain

**Response Example**:
```json
{
  "nameServersToResources": {
    "remote-ns-net-1": {
      "netId": "remote-ns-net-1",
      "lastSeen": 1715668500000,
      "resourceCount": 1,
      "resourceMap": {
        "resource-1": { ... }
      }
    }
  },
  "count": 1,
  "timestamp": 1715668512345
}
```

**HTTP Status**: 200 OK

---

### 5. Export All Collections

**Endpoint**: `GET /resources-export/all`

Exports all three collections (resources, netToResources, nameServersToResources) in a single response.

**Query Parameters**:
- `domain` (optional): Specify nameserver domain

**Response Example**:
```json
{
  "resources": { ... },
  "netToResources": { ... },
  "nameServersToResources": { ... },
  "exportTimestamp": 1715668512345
}
```

**HTTP Status**: 200 OK

---

## Swagger Integration

### Overview

Swagger (OpenAPI) provides an interactive UI to explore and test API endpoints. This section explains how to integrate Swagger with the NameServer Resources Export API.

### Prerequisites

- Swagger UI library (springdoc-openapi or similar)
- OpenAPI 3.0 compatible annotations
- Gradle configuration update

### Option 1: Add Springdoc-OpenAPI (Recommended for Spring apps)

If using Spring Boot, add to `build.gradle`:

```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.0'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-api:2.0.0'
}
```

Then add to application startup:

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/resources-export/**")
            .build();
    }
}
```

### Option 2: Manual Swagger UI Setup (Jakarta Servlets)

Since this project uses Jakarta Servlets directly (not Spring), perform these steps:

#### Step 1: Add Swagger Core Dependencies

Update `core/core/build.gradle`:

```gradle
dependencies {
    implementation 'io.swagger.core.v3:swagger-core:2.2.0'
    implementation 'io.swagger.core.v3:swagger-jaxrs2:2.2.0'
    implementation 'org.glassfish.jersey.containers:jersey-container-servlet:3.1.0'
    implementation 'org.glassfish.jersey.media:jersey-media-json-jackson:3.1.0'
}
```

#### Step 2: Create OpenAPI Configuration Servlet

Create file: `core/core/src/main/java/gov/ic/silkwave/web/OpenAPIConfigServlet.java`

```java
package gov.ic.silkwave.web;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.paths.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import java.io.Serial;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OpenAPIConfigServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        
        OpenAPI openAPI = new OpenAPI()
            .info(new Info()
                .title("NameServer Resources Export API")
                .version("1.0.0")
                .description("REST API for exporting NameServer resource collections"));

        // Set base server
        openAPI.addServersItem(new Server()
            .url("http://localhost:8080")
            .description("Local Development Server"));

        // Response output
        response.setContentType("application/yaml");
        try {
            response.getWriter().write(Yaml.pretty(openAPI));
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
```

#### Step 3: Register OpenAPI Servlet in WebServer

Update `WebServer.java` line 93-95:

```java
context.addServlet(new ServletHolder(new ResourcesExportServlet()), "/resources-export/*");
context.addServlet(new ServletHolder(new OpenAPIConfigServlet()), "/v3/api-docs");
```

#### Step 4: Add Static Swagger UI Files

Create directory: `core/core/src/main/webapp/swagger-ui/`

Download from: https://github.com/swagger-api/swagger-ui/releases

Add these files:
- `index.html`
- `swagger-ui.css`
- `swagger-ui.js`
- `swagger-ui-bundle.js`
- `swagger-ui-standalone-preset.js`

Modify `index.html` to point to your API:
```html
<script>
  url: "http://localhost:8080/v3/api-docs"
</script>
```

#### Step 5: Register Static Content in WebServer

```java
context.setResourceBase("src/main/webapp");
context.addServlet(new ServletHolder(new DefaultServlet()), "/swagger-ui/*");
```

### Option 3: Using Online Swagger Editor

Don't want to modify the project? Use the online Swagger Editor:

1. Go to: https://editor.swagger.io/
2. Create or paste your OpenAPI specification
3. Use the "Servers" section to set your API base URL

**Example OpenAPI Spec:**

```yaml
openapi: 3.0.0
info:
  title: NameServer Resources Export API
  version: 1.0.0
servers:
  - url: http://localhost:8080
    description: Local Development Server

paths:
  /resources-export/:
    get:
      summary: List available domains
      operationId: listDomains
      responses:
        '200':
          description: List of domains and endpoints
          content:
            application/json:
              schema:
                type: object
                properties:
                  domains:
                    type: array
                    items:
                      type: string
                  endpoints:
                    type: array
                    items:
                      type: string

  /resources-export/resources:
    get:
      summary: Export all resource routes
      operationId: exportResources
      parameters:
        - name: domain
          in: query
          required: false
          schema:
            type: string
      responses:
        '200':
          description: All resource routes
          content:
            application/json:
              schema:
                type: object
        '404':
          description: Domain not found

  /resources-export/netResources:
    get:
      summary: Export local net to resources mapping
      operationId: exportNetResources
      parameters:
        - name: domain
          in: query
          required: false
          schema:
            type: string
      responses:
        '200':
          description: Net to resources mapping

  /resources-export/nameServerResources:
    get:
      summary: Export remote nameserver resources
      operationId: exportNameServerResources
      parameters:
        - name: domain
          in: query
          required: false
          schema:
            type: string
      responses:
        '200':
          description: NameServer resources mapping

  /resources-export/all:
    get:
      summary: Export all collections
      operationId: exportAll
      parameters:
        - name: domain
          in: query
          required: false
          schema:
            type: string
      responses:
        '200':
          description: All three collections combined
```

### Accessing Swagger UI

Once configured, access via:

```
http://localhost:8080/swagger-ui/
```

Or for online editor:
```
https://editor.swagger.io/?url=http://localhost:8080/v3/api-docs
```

### Using Swagger UI

1. **Load API Spec**: UI automatically loads from `/v3/api-docs`
2. **Browse Endpoints**: See all 5 endpoints listed
3. **Try it Out**: Click "Try it out" on any endpoint
4. **Set Parameters**: Fill in optional `domain` parameter
5. **Execute**: Click "Execute" to test the endpoint
6. **View Response**: See JSON response and HTTP status

---

## Example Usage

### Using cURL

#### List Domains
```bash
curl -X GET http://localhost:8080/resources-export/
```

#### Export All Resources
```bash
curl -X GET http://localhost:8080/resources-export/resources
```

#### Export with Specific Domain
```bash
curl -X GET "http://localhost:8080/resources-export/resources?domain=example.com"
```

#### Pretty-print JSON Response
```bash
curl -X GET http://localhost:8080/resources-export/all | jq .
```

### Using Python

```python
import requests
import json

BASE_URL = "http://localhost:8080/resources-export"

# List domains
response = requests.get(f"{BASE_URL}/")
print(json.dumps(response.json(), indent=2))

# Export all resources
response = requests.get(f"{BASE_URL}/resources?domain=example.com")
resources = response.json()
print(f"Found {resources['count']} resources")

# Export everything
response = requests.get(f"{BASE_URL}/all")
all_data = response.json()
print(f"Resources: {len(all_data['resources'])}")
print(f"Local nets: {len(all_data['netToResources'])}")
print(f"Remote nameservers: {len(all_data['nameServersToResources'])}")
```

### Using JavaScript/Node.js

```javascript
const BASE_URL = "http://localhost:8080/resources-export";

// List domains
fetch(`${BASE_URL}/`)
  .then(res => res.json())
  .then(data => console.log("Available domains:", data.domains));

// Export all resources for a domain
fetch(`${BASE_URL}/resources?domain=example.com`)
  .then(res => res.json())
  .then(data => {
    console.log(`Found ${data.count} resources`);
    Object.entries(data.resources).forEach(([id, route]) => {
      console.log(`- ${id}: ${route.type}`);
    });
  });

// Export everything
fetch(`${BASE_URL}/all`)
  .then(res => res.json())
  .then(data => {
    console.log("Export timestamp:", new Date(data.exportTimestamp));
    console.log("Resources count:", Object.keys(data.resources).length);
  });
```

### Using Postman

1. **Import Collection**: Create new request
2. **Method**: Select "GET"
3. **URL**: `http://localhost:8080/resources-export/all`
4. **Query Params**: Add `domain` = `example.com` (optional)
5. **Headers**: (none required, Content-Type: application/json automatic)
6. **Send**: Click Send button
7. **Response**: View JSON response in "Body" tab

---

## Troubleshooting

### Common Issues

#### Issue 1: API Returns 404 (Not Found)

**Symptoms**: 
```
curl: (7) Failed to connect to localhost port 8080: Connection refused
```

**Solution**:
1. Verify application is running
2. Check the port (default is 8080)
3. Verify API path is correct: `/resources-export/`

```bash
# Test health endpoint first
curl http://localhost:8080/health

# Then test API
curl http://localhost:8080/resources-export/
```

#### Issue 2: No NameServers Available

**Symptoms**:
```json
{
  "error": "No nameservers available",
  "status": 404
}
```

**Solution**:
1. Verify at least one NameServer is initialized
2. Check NameServerLocator.getNameServerDomains() returns non-empty list
3. Ensure NameServer startup was successful

#### Issue 3: Domain Not Found

**Symptoms**:
```json
{
  "error": "NameServer not found for domain: invalid.domain",
  "status": 404
}
```

**Solution**:
1. List available domains: `GET /resources-export/`
2. Use a domain from the returned list
3. Check domain name spelling (case-sensitive)

```bash
# List available domains
curl http://localhost:8080/resources-export/ | jq .domains

# Use one of the listed domains
curl "http://localhost:8080/resources-export/resources?domain=CORRECT_DOMAIN"
```

#### Issue 4: Empty Response (No Resources)

**Symptoms**:
```json
{
  "resources": {},
  "count": 0,
  "timestamp": 1715668512345
}
```

**Explanation**: This is normal if no resources have been registered yet.

**Solution**:
1. Register a resource with the NameServer first
2. Wait for resources to propagate
3. Then query the API

#### Issue 5: Swagger UI Not Loading

**Symptoms**: 404 error when accessing `http://localhost:8080/swagger-ui/`

**Solution**:
1. Verify Swagger dependencies are installed
2. Check servlet registration in WebServer
3. Verify static files are in correct location
4. Use online Swagger Editor as workaround

```bash
# Check if Swagger servlet is registered
grep -r "swagger-ui" core/core/src/main/java/gov/ic/silkwave/web/WebServer.java
```

#### Issue 6: Thread Lock Timeout

**Symptoms**: Request hangs for 30+ seconds then times out

**Explanation**: The read lock may be blocked by a write operation

**Solution**:
1. Check for long-running write operations on NameServer
2. Verify no deadlocks in resource modification code
3. Monitor thread activity
4. Increase timeout if necessary

### Debug Logging

Enable debug logging in your application:

```properties
# In application.properties or similar
logging.level.gov.ic.silkwave.web.ResourcesExportServlet=DEBUG
logging.level.gov.ic.silkwave.nameserver.NameServer=DEBUG
```

Then check logs for:
```
[DEBUG] ResourcesExportServlet: Processing GET /resources-export/resources
[DEBUG] NameServer: Acquiring read lock
[DEBUG] NameServer: Creating defensive copy
[DEBUG] ResourcesExportServlet: Sending 200 response
```

### Performance Issues

**Symptom**: API responds slowly for large datasets

**Optimization Tips**:

1. **Query Specific Domain**: Avoid querying all domains
   ```bash
   # Slower (all domains)
   curl http://localhost:8080/resources-export/resources
   
   # Faster (specific domain)
   curl "http://localhost:8080/resources-export/resources?domain=example.com"
   ```

2. **Use Specific Endpoints**: Don't always use `/all`
   ```bash
   # If you only need resources, use:
   curl http://localhost:8080/resources-export/resources
   # Instead of:
   curl http://localhost:8080/resources-export/all
   ```

3. **Implement Client-Side Caching**: Cache responses to avoid repeated requests
   ```python
   from functools import lru_cache
   import requests
   
   @lru_cache(maxsize=32)
   def get_resources(domain):
       response = requests.get(f"http://localhost:8080/resources-export/resources?domain={domain}")
       return response.json()
   ```

4. **Consider Pagination**: For very large responses, implement pagination at client level

---

## Additional Resources

### Files Reference

| File | Location | Purpose |
|------|----------|---------|
| NetIdDTO.java | `core/silkwave.nameserver/.../dto/` | DTO for NetId |
| ResourceRouteDTO.java | `core/silkwave.nameserver/.../dto/` | DTO for ResourceRoute |
| NetResourcesDTO.java | `core/silkwave.nameserver/.../dto/` | DTO for NetResources |
| ResourcesExportDTO.java | `core/silkwave.nameserver/.../dto/` | Top-level DTO |
| ResourcesExportServlet.java | `core/core/.../web/` | REST API Servlet |
| NameServer.java | `core/silkwave.nameserver/.../` | Modified (getters added) |
| WebServer.java | `core/core/.../web/` | Modified (servlet registered) |

### Related Documentation

- Jakarta Servlet Specification: https://jakarta.ee/specifications/servlet/
- GSON Documentation: https://github.com/google/gson
- OpenAPI/Swagger: https://swagger.io/specification/
- Thread Safety in Java: https://docs.oracle.com/javase/tutorial/essential/concurrency/

---

## Support & Contact

For issues or questions regarding this API:

1. **Check Logs**: Review application logs for error messages
2. **Test Endpoints**: Use cURL or Postman to verify functionality
3. **Review Documentation**: Consult this guide
4. **Enable Debug Logging**: See Troubleshooting section
5. **Check Thread Dumps**: If experiencing hangs, generate thread dumps

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | May 14, 2026 | Initial API implementation |

---

**Document Status**: Complete and Ready for Production  
**Last Updated**: May 14, 2026  
**Maintained By**: Development Team
