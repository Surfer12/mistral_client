/**
 * Transform cognitive analysis response
 */
function transformResponse() {
    try {
        // Get response from MCP tool
        var mcpResponse = context.getVariable('response.content');
        var cognitiveData = JSON.parse(mcpResponse);
        
        // Transform thought nodes
        var transformedData = {
            analysisId: context.getVariable('messageid'),
            timestamp: moment().toISOString(),
            cognitiveModel: {
                nodes: transformThoughtNodes(cognitiveData.nodes),
                metadata: {
                    nodeCount: cognitiveData.nodes ? cognitiveData.nodes.length : 0,
                    processingTime: context.getVariable('target.response.time'),
                    concurrent: cognitiveData.concurrent || false,
                    cacheEnabled: cognitiveData.cacheEnabled || false
                }
            },
            insights: cognitiveData.insights || [],
            performance: {
                executionTimeMs: parseInt(context.getVariable('target.response.time')),
                cacheHits: cognitiveData.cacheHits || 0,
                nodeProcessingRate: calculateProcessingRate(cognitiveData)
            }
        };
        
        // Set transformed response
        context.setVariable('response.content', JSON.stringify(transformedData));
        context.setVariable('response.header.Content-Type', 'application/json');
    } catch (e) {
        context.setVariable('error.message', 'Error transforming cognitive response: ' + e.message);
        context.setVariable('error.code', '500');
    }
}

/**
 * Transform thought nodes into standardized format
 */
function transformThoughtNodes(nodes) {
    if (!nodes || !Array.isArray(nodes)) return [];
    
    return nodes.map(node => ({
        id: node.id,
        type: 'thought_node',
        attributes: {
            description: node.description,
            hasAlias: node.hasAliasReference || false,
            subThoughtCount: (node.subThoughts || []).length,
            cachedInsight: node.cachedInsight || null
        },
        relationships: {
            subThoughts: (node.subThoughts || []).map(st => ({
                id: st.id,
                type: 'thought_node'
            })),
            aliasReference: node.aliasReference ? {
                id: node.aliasReference.id,
                type: 'thought_node'
            } : null
        }
    }));
}

/**
 * Calculate node processing rate
 */
function calculateProcessingRate(data) {
    if (!data.nodes || !data.nodes.length) return 0;
    var executionTime = parseInt(context.getVariable('target.response.time'));
    return executionTime > 0 ? (data.nodes.length / (executionTime / 1000)).toFixed(2) : 0;
}

// Execute transformation
transformResponse(); 