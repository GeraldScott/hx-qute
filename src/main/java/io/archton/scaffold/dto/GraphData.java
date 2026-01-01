package io.archton.scaffold.dto;

import java.util.List;

public record GraphData(
    List<GraphNode> nodes,
    List<GraphLink> links
) {}
