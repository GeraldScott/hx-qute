package io.archton.scaffold.dto;

import java.util.List;

/**
 * Root DTO for Cytoscape.js graph data.
 * Returns { nodes: [{data: {...}}, ...], edges: [{data: {...}}, ...], relationshipTypes: [...] }
 */
public record GraphData(
    List<CyNode> nodes,
    List<CyEdge> edges,
    List<String> relationshipTypes  // For filter dropdown
) {
    /**
     * Wrapper for Cytoscape node format.
     * Cytoscape expects: { data: { id, name, ... } }
     */
    public record CyNode(GraphNode data) {
        public static CyNode from(GraphNode node) {
            return new CyNode(node);
        }
    }

    /**
     * Wrapper for Cytoscape edge format.
     * Cytoscape expects: { data: { id, source, target, label } }
     */
    public record CyEdge(GraphEdge data) {
        public static CyEdge from(GraphEdge edge) {
            return new CyEdge(edge);
        }
    }
}
