(function() {
    'use strict';

    // Configuration
    const CONFIG = {
        nodeRadius: { min: 8, max: 25 },
        colors: {
            female: '#FF69B4',  // Hot Pink
            male: '#4169E1',    // Royal Blue
            unspecified: '#808080'  // Gray
        },
        forces: {
            charge: -150,
            linkDistance: 60,
            collideRadius: 20,
            centerStrength: 0.1
        },
        animation: {
            alphaTarget: 0.3,
            alphaMin: 0.001,
            velocityDecay: 0.4
        }
    };

    // State
    let simulation;
    let svg, g, link, node, nodeCircles, labels;
    let zoom;
    let graphData = { nodes: [], links: [] };
    let maxConnections = 1;

    // Initialize graph
    async function init() {
        const container = document.getElementById('graph-container');
        const svgElement = document.getElementById('graph-svg');
        const width = container.clientWidth;
        const height = container.clientHeight;

        // Setup SVG with zoom
        svg = d3.select('#graph-svg');
        g = svg.append('g');

        zoom = d3.zoom()
            .scaleExtent([0.1, 4])
            .on('zoom', (event) => {
                g.attr('transform', event.transform);
            });

        svg.call(zoom);

        // Fetch data
        try {
            const response = await fetch('/graph/data', {
                credentials: 'same-origin'
            });
            graphData = await response.json();

            if (graphData.nodes.length === 0) {
                document.getElementById('graph-empty').style.display = 'flex';
                document.getElementById('graph-svg').style.display = 'none';
                return;
            }

            initSimulation(width, height);
            renderGraph();
            setupEventHandlers();
        } catch (error) {
            console.error('Failed to load graph data:', error);
        }
    }

    // Initialize force simulation
    function initSimulation(width, height) {
        // Cache maxConnections for getNodeRadius performance
        maxConnections = Math.max(...graphData.nodes.map(n => n.relationshipCount), 1);

        simulation = d3.forceSimulation(graphData.nodes)
            .force('link', d3.forceLink(graphData.links)
                .id(d => d.id)
                .distance(CONFIG.forces.linkDistance))
            .force('charge', d3.forceManyBody()
                .strength(CONFIG.forces.charge))
            .force('center', d3.forceCenter(width / 2, height / 2)
                .strength(CONFIG.forces.centerStrength))
            .force('collide', d3.forceCollide()
                .radius(d => getNodeRadius(d) + 5))
            .alphaMin(CONFIG.animation.alphaMin)
            .velocityDecay(CONFIG.animation.velocityDecay)
            .on('tick', ticked);
    }

    // Render graph elements
    function renderGraph() {
        // Links (edges)
        link = g.append('g')
            .attr('class', 'links')
            .selectAll('line')
            .data(graphData.links)
            .join('line')
            .attr('stroke', '#999')
            .attr('stroke-opacity', 0.6)
            .attr('stroke-width', 2);

        // Link tooltips
        link.append('title')
            .text(d => d.relationshipType);

        // Nodes (wrapped in groups for proper tooltip support)
        node = g.append('g')
            .attr('class', 'nodes')
            .selectAll('g')
            .data(graphData.nodes)
            .join('g')
            .call(drag(simulation))
            .on('click', handleNodeClick)
            .on('mouseover', handleNodeHover)
            .on('mouseout', handleNodeUnhover);

        // Context menu (optional - requires d3-context-menu plugin)
        if (typeof d3.contextMenu === 'function') {
            node.on('contextmenu', d3.contextMenu(getContextMenu));
        }

        // Node circles (inside groups)
        nodeCircles = node.append('circle')
            .attr('r', d => getNodeRadius(d))
            .attr('fill', d => getNodeColor(d))
            .attr('stroke', '#fff')
            .attr('stroke-width', 2);

        // Node tooltips (now works because parent is a group, not circle)
        node.append('title')
            .text(d => `${d.firstName} ${d.lastName}\n${d.email}`);

        // Labels
        labels = g.append('g')
            .attr('class', 'labels')
            .selectAll('text')
            .data(graphData.nodes)
            .join('text')
            .text(d => `${d.firstName} ${d.lastName}`)
            .attr('font-size', 10)
            .attr('dx', d => getNodeRadius(d) + 5)
            .attr('dy', 4);
    }

    // Update positions on tick
    function ticked() {
        if (!link || !node || !labels) return;

        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        // Use transform for node groups (not cx/cy since nodes are now <g> elements)
        node.attr('transform', d => `translate(${d.x},${d.y})`);

        labels
            .attr('x', d => d.x)
            .attr('y', d => d.y);
    }

    // Drag behavior with spring-back
    function drag(simulation) {
        function dragstarted(event, d) {
            if (!event.active) simulation.alphaTarget(CONFIG.animation.alphaTarget).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(event, d) {
            d.fx = event.x;
            d.fy = event.y;
        }

        function dragended(event, d) {
            if (!event.active) simulation.alphaTarget(0);
            // Spring back: unfix position
            d.fx = null;
            d.fy = null;
        }

        return d3.drag()
            .on('start', dragstarted)
            .on('drag', dragged)
            .on('end', dragended);
    }

    // Get node color based on gender
    function getNodeColor(d) {
        switch (d.genderCode) {
            case 'F': return CONFIG.colors.female;
            case 'M': return CONFIG.colors.male;
            default: return CONFIG.colors.unspecified;
        }
    }

    // Get node radius based on relationship count (uses cached maxConnections)
    function getNodeRadius(d) {
        const { min, max } = CONFIG.nodeRadius;
        return min + (d.relationshipCount / maxConnections) * (max - min);
    }

    // Context menu definition
    function getContextMenu(d) {
        return [
            {
                title: 'View Details',
                action: function(d, event) {
                    loadPersonModal(d.id);
                }
            },
            { divider: true },
            {
                title: 'Manage Relationships',
                action: function(d, event) {
                    window.location.href = `/persons/${d.id}/relationships`;
                }
            }
        ];
    }

    // Load person details into modal
    function loadPersonModal(personId) {
        fetch(`/graph/person/${personId}`, {
            credentials: 'same-origin'
        })
            .then(response => response.text())
            .then(html => {
                document.getElementById('person-modal-content').innerHTML = html;
                UIkit.modal('#person-modal').show();
            });
    }

    // Handle node click - neighborhood highlighting
    function handleNodeClick(event, d) {
        event.stopPropagation();

        // Get connected node IDs
        const connectedIds = new Set([d.id]);
        graphData.links.forEach(l => {
            if (l.source.id === d.id) connectedIds.add(l.target.id);
            if (l.target.id === d.id) connectedIds.add(l.source.id);
        });

        // Highlight nodes
        node.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);

        // Highlight links
        link.attr('opacity', l =>
            (l.source.id === d.id || l.target.id === d.id) ? 1 : 0.1
        );

        // Highlight labels
        labels.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);
    }

    // Handle node hover (select circle inside the group)
    function handleNodeHover(event, d) {
        d3.select(this).select('circle')
            .attr('stroke-width', 4)
            .attr('stroke', '#333');
    }

    function handleNodeUnhover(event, d) {
        d3.select(this).select('circle')
            .attr('stroke-width', 2)
            .attr('stroke', '#fff');
    }

    // Setup event handlers
    function setupEventHandlers() {
        // Click on background to reset highlight
        svg.on('click', () => {
            node.attr('opacity', 1);
            link.attr('opacity', 0.6);
            labels.attr('opacity', 1);
        });

        // Search
        const searchInput = document.getElementById('graph-search');
        let searchTimeout;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                filterBySearch(e.target.value);
            }, 300);
        });

        // Relationship filter
        document.getElementById('relationship-filter').addEventListener('change', (e) => {
            filterByRelationship(e.target.value);
        });

        // Reset view
        document.getElementById('reset-graph').addEventListener('click', resetView);
    }

    // Filter nodes by search term
    function filterBySearch(term) {
        const searchTerm = term.toLowerCase().trim();

        if (!searchTerm) {
            node.attr('opacity', 1);
            link.attr('opacity', 0.6);
            labels.attr('opacity', 1);
            return;
        }

        const matchingIds = new Set(
            graphData.nodes
                .filter(n =>
                    n.firstName.toLowerCase().includes(searchTerm) ||
                    n.lastName.toLowerCase().includes(searchTerm))
                .map(n => n.id)
        );

        node.attr('opacity', n => matchingIds.has(n.id) ? 1 : 0.2);
        labels.attr('opacity', n => matchingIds.has(n.id) ? 1 : 0.2);
    }

    // Filter by relationship type
    function filterByRelationship(code) {
        if (!code) {
            node.attr('opacity', 1);
            link.attr('opacity', 0.6);
            labels.attr('opacity', 1);
            return;
        }

        const matchingLinks = graphData.links.filter(l => l.relationshipCode === code);
        const connectedIds = new Set();
        matchingLinks.forEach(l => {
            connectedIds.add(l.source.id);
            connectedIds.add(l.target.id);
        });

        node.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);
        link.attr('opacity', l => l.relationshipCode === code ? 0.8 : 0.1);
        labels.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);
    }

    // Reset view to initial state
    function resetView() {
        // Reset zoom
        svg.transition().duration(500).call(
            zoom.transform,
            d3.zoomIdentity
        );

        // Reset filters
        document.getElementById('graph-search').value = '';
        document.getElementById('relationship-filter').value = '';

        // Reset opacity
        node.attr('opacity', 1);
        link.attr('opacity', 0.6);
        labels.attr('opacity', 1);

        // Reheat simulation
        simulation.alpha(0.3).restart();
    }

    // Initialize on DOM ready
    document.addEventListener('DOMContentLoaded', init);
})();
