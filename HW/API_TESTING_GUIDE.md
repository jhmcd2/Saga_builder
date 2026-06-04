# API Testing Guide for NameServer Resources Export

## API Endpoints

All endpoints are mounted at `/resources-export/` base path.

### 1. List Available Domains
**Endpoint**: `GET /resources-export/`

**Description**: Lists all available nameserver domains and valid endpoints

**Response Example**:
```json
{
  "domains": ["example.com", "test.domain"],
  "endpoints": ["/resources", "/netResources", "/nameServerResources", "/all"]
}
```

### 2. Export All Resource Routes
**Endpoint**: `GET /resources-export/resources?domain=example.com`

**Description**: Exports all known resource routes (local + remote)

**Query Parameters**:
- `domain` (optional): Specify which nameserver domain to query. If not provided, uses first available.

**Response Example**:
```json
{
  "resources": {
    "resource-id-1": {
      "resId": "resource-id-1",
      "type": "SERVICE",
      "netIds": {
        "net-addr-1": {
          "id": "net-addr-1",
          "ownerId": "owner-1",
          "creationTime": 1715668500000
        }
      }
    }
  },
  "count": 1,
  "timestamp": 1715668512345
}
```

### 3. Export Net To Resources Mapping
**Endpoint**: `GET /resources-export/netResources?domain=example.com`

**Description**: Exports mapping of local net IDs to their NetResources

**Query Parameters**:
- `domain` (optional): Specify which nameserver domain to query.

**Response Example**:
```json
{
  "netToResources": {
    "net-local-1": {
      "netId": "net-local-1",
      "lastSeen": 1715668510000,
      "resourceCount": 2,
      "resourceMap": {
        "resource-1": {
          "resId": "resource-1",
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

### 4. Export NameServer To Resources Mapping
**Endpoint**: `GET /resources-export/nameServerResources?domain=example.com`

**Description**: Exports mapping of other nameservers' net IDs to their NetResources

**Query Parameters**:
- `domain` (optional): Specify which nameserver domain to query.

**Response Example**:
```json
{
  "nameServersToResources": {
    "remote-ns-net-1": {
      "netId": "remote-ns-net-1",
      "lastSeen": 1715668500000,
      "resourceCount": 1,
      "resourceMap": { ... }
    }
  },
  "count": 1,
  "timestamp": 1715668512345
}
```

### 5. Export All Collections
**Endpoint**: `GET /resources-export/all?domain=example.com`

**Description**: Exports all three collections (resources, netToResources, nameServersToResources) in a single response

**Query Parameters**:
- `domain` (optional): Specify which nameserver domain to query.

**Response Example**:
```json
{
  "resources": { ... },
  "netToResources": { ... },
  "nameServersToResources": { ... },
  "exportTimestamp": 1715668512345
}
```

## Error Responses

### Domain Not Found
**Status Code**: 404

**Response**:
```json
{
  "error": "NameServer not found for domain: invalid.domain",
  "status": 404,
  "timestamp": 1715668512345
}
```

### No NameServers Available
**Status Code**: 404

**Response**:
```json
{
  "error": "No nameservers available",
  "status": 404,
  "timestamp": 1715668512345
}
```

### Server Error
**Status Code**: 500

**Response**:
```json
{
  "error": "Internal server error: [error message]",
  "status": 500,
  "timestamp": 1715668512345
}
```

## cURL Examples

```bash
# List available domains
curl http://localhost:8080/resources-export/

# Export all resources
curl http://localhost:8080/resources-export/resources?domain=example.com

# Export net to resources mapping
curl http://localhost:8080/resources-export/netResources?domain=example.com

# Export nameserver to resources mapping
curl http://localhost:8080/resources-export/nameServerResources?domain=example.com

# Export everything at once
curl http://localhost:8080/resources-export/all?domain=example.com
```

## Thread Safety

All endpoints use read locks from the NameServer's ReentrantReadWriteLock to ensure thread-safe access to the concurrent maps. The servlet creates defensive copies of the data to prevent concurrent modification issues.

## Performance Considerations

- **Large Datasets**: For systems with many resources, the `/all` endpoint may return large JSON payloads
- **Network Performance**: Consider pagination or filtering at the client level for large result sets
- **Caching**: Responses are generated fresh on each request; consider implementing client-side caching if needed
