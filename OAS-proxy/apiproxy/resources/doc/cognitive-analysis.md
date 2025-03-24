# Cognitive Analysis API Documentation

## Overview
The Cognitive Analysis API provides endpoints for processing and analyzing cognitive data using the Model Context Protocol (MCP) tools. This implementation integrates the CCT (Cognitive Chain of Thought) model for advanced thought processing and analysis.

## Endpoints

### POST /v1/cognitive/analyze
Analyzes cognitive data using MCP tools and returns processed insights.

#### Request Format
```json
{
  "nodes": [
    {
      "id": "integer",
      "description": "string",
      "subThoughts": ["array of node references"],
      "aliasReference": "optional node reference"
    }
  ],
  "options": {
    "concurrent": "boolean",
    "cacheEnabled": "boolean",
    "maxNodes": "integer"
  }
}
```

#### Response Format
```json
{
  "analysisId": "string",
  "timestamp": "ISO8601 string",
  "cognitiveModel": {
    "nodes": [
      {
        "id": "integer",
        "type": "thought_node",
        "attributes": {
          "description": "string",
          "hasAlias": "boolean",
          "subThoughtCount": "integer",
          "cachedInsight": "string|null"
        },
        "relationships": {
          "subThoughts": ["array of node references"],
          "aliasReference": "optional node reference"
        }
      }
    ],
    "metadata": {
      "nodeCount": "integer",
      "processingTime": "integer",
      "concurrent": "boolean",
      "cacheEnabled": "boolean"
    }
  },
  "insights": ["array of strings"],
  "performance": {
    "executionTimeMs": "integer",
    "cacheHits": "integer",
    "nodeProcessingRate": "float"
  }
}
```

## Configuration

### MCP Tool Settings
- `mcpToolClass`: CCT tool implementation class
- `mcpModelClass`: CCT model implementation class
- `thoughtNodeClass`: Thought node model class
- `enableConcurrent`: Enable concurrent processing
- `cacheEnabled`: Enable response caching
- `maxNodes`: Maximum nodes per request

### Monitoring
The API includes comprehensive monitoring:
- Execution time tracking
- Cache hit rates
- Node processing metrics
- Error tracking
- Resource utilization

## Error Handling
- 400: Invalid request format
- 422: Invalid cognitive data structure
- 429: Rate limit exceeded
- 500: Internal processing error
- 504: Processing timeout

## Best Practices
1. Use concurrent processing for large node sets
2. Enable caching for repeated analysis
3. Monitor performance metrics
4. Handle timeout scenarios
5. Implement proper error handling

## Examples

### Basic Analysis Request
```curl
curl -X POST https://api.example.com/v1/cognitive/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "nodes": [
      {
        "id": 1,
        "description": "Root thought",
        "subThoughts": []
      }
    ],
    "options": {
      "concurrent": true,
      "cacheEnabled": true
    }
  }'
``` 