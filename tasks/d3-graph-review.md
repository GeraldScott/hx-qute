# D3.js Network Graph Review - graph.js Analysis

**Date:** 2026-01-02
**Status:** Implementing via Option A (fix during UC implementation)
**File:** `src/main/resources/META-INF/resources/js/graph.js`

**Implementation Plan:**
| Issue | Fix During | Status |
|-------|------------|--------|
| SVG `<title>` fix (nodes) | UC-004-01-02 | ✅ Done |
| Cache `maxConnections` | UC-004-01-02 | ✅ Done |
| Modal error handling | UC-004-01-03 | 🔲 Pending |
| Email search filter | UC-004-01-04 | 🔲 Pending |
| SVG `<title>` fix (edges) | UC-004-01-06 | 🔲 Pending |
| Visibility API, CONFIG cleanup, Low priority | Post UC-004-01-06 | 🔲 Pending |

---

## Executive Summary

Code review conducted against D3.js v7 best practices using Context7 documentation. The implementation follows many modern D3 patterns correctly (IIFE encapsulation, modern event syntax, proper drag/zoom behavior). However, there is a critical SVG structure bug preventing tooltips from working, plus several performance and error handling improvements needed.

---

## Todo List

### Critical Priority

- [x] **Fix SVG `<title>` elements on non-container elements (Lines 101, 125)** ✅ Fixed in UC-004-01-02
  - SVG `<circle>` and `<line>` cannot contain child elements
  - Tooltips are not rendering as expected
  - Solution: Wrap nodes/links in `<g>` groups, or use external tooltip library
  - Source: SVG specification, D3 best practices

### High Priority

- [ ] **Add error handling to modal fetch (Lines 222-230)**
  - `loadPersonModal()` has no `.catch()` handler
  - Failed requests fail silently with no user feedback
  - Add error handling and user notification

- [x] **Cache `maxConnections` calculation (Lines 195-199)** ✅ Fixed in UC-004-01-02
  - `getNodeRadius()` recalculates `Math.max(...graphData.nodes.map(...))` on every call
  - O(n) operation called multiple times during render
  - Move calculation to `initSimulation()` and cache the value

### Medium Priority

- [ ] **Add visibility API for simulation performance**
  - Simulation continues running when browser tab is hidden
  - Wastes CPU cycles when user isn't viewing the page
  - Add `document.visibilitychange` listener to stop/restart simulation

- [ ] **Extract magic numbers to CONFIG object**
  - Opacity values hardcoded throughout: `0.2`, `0.1`, `0.6`, `1`
  - Link stroke values: `#999`, `2`
  - Add `CONFIG.opacity` and `CONFIG.link` sections

- [ ] **Add email to search filter (Lines 308-312)**
  - Currently only searches firstName and lastName
  - Email is available in node data but not searchable
  - Add optional email matching

### Low Priority

- [ ] **Remove unused variable `svgElement` (Line 34)**
  - Declared but never used
  - Clean up for code clarity

- [ ] **Remove redundant `credentials: 'same-origin'` (Lines 53, 223)**
  - This is the default fetch behavior
  - Can be removed for cleaner code

- [ ] **Add `alphaDecay` to CONFIG (Lines 72-86)**
  - D3 default is 0.0228
  - Documenting it improves maintainability and tuning

- [ ] **Consider case-insensitive gender code comparison (Lines 187-191)**
  - Currently exact match: `'F'`, `'M'`
  - Could handle lowercase variants

---

## Detailed Findings

### Correct Patterns Used

| Pattern | Lines | Assessment |
|---------|-------|------------|
| IIFE encapsulation | 1-363 | Prevents global scope pollution |
| `'use strict'` mode | 2 | Catches common JavaScript errors |
| Configuration object | 5-23 | Centralizes tunable values |
| Modern D3 event syntax | 44, 161-182, 233 | Uses `(event, d)` pattern (D3 v6+) |
| `.join()` for data binding | 95, 109, 133 | Preferred over enter/update/exit pattern |
| Drag with `fx`/`fy` nullification | 172-176 | Correct spring-back behavior |
| Zoom reset with `d3.zoomIdentity` | 343-346 | Proper zoom reset pattern |
| Debounced search input | 281-285 | Prevents excessive filtering calls |
| Defensive null checks in `ticked()` | 142 | Guards against undefined elements |

### Critical Issue: SVG Structure Bug

**Problem (Lines 101-102, 125-126):**
```javascript
link.append('title')
    .text(d => d.relationshipType);

node.append('title')
    .text(d => `${d.firstName} ${d.lastName}\n${d.email}`);
```

SVG `<line>` and `<circle>` are non-container elements per the SVG specification. They cannot contain child elements like `<title>`. The browser silently ignores these, so tooltips don't appear.

**Solution - Wrap in Groups:**
```javascript
// Nodes wrapped in groups
node = g.append('g')
    .attr('class', 'nodes')
    .selectAll('g')
    .data(graphData.nodes)
    .join('g')
    .attr('transform', d => `translate(${d.x},${d.y})`)
    .call(drag(simulation))
    .on('click', handleNodeClick)
    .on('mouseover', handleNodeHover)
    .on('mouseout', handleNodeUnhover);

node.append('circle')
    .attr('r', d => getNodeRadius(d))
    .attr('fill', d => getNodeColor(d))
    .attr('stroke', '#fff')
    .attr('stroke-width', 2);

node.append('title')
    .text(d => `${d.firstName} ${d.lastName}\n${d.email}`);
```

**Note:** This requires updating `ticked()` to use `transform` instead of `cx`/`cy`.

### Performance Issue: Repeated Max Calculation

**Problem (Lines 195-199):**
```javascript
function getNodeRadius(d) {
    const { min, max } = CONFIG.nodeRadius;
    const maxConnections = Math.max(...graphData.nodes.map(n => n.relationshipCount), 1);
    return min + (d.relationshipCount / maxConnections) * (max - min);
}
```

This O(n) operation runs for every node during rendering and on every tick.

**Solution:**
```javascript
let maxConnections = 1;

function initSimulation(width, height) {
    maxConnections = Math.max(...graphData.nodes.map(n => n.relationshipCount), 1);
    // ... rest of simulation setup
}

function getNodeRadius(d) {
    const { min, max } = CONFIG.nodeRadius;
    return min + (d.relationshipCount / maxConnections) * (max - min);
}
```

### Missing Error Handling

**Problem (Lines 222-230):**
```javascript
function loadPersonModal(personId) {
    fetch(`/graph/person/${personId}`, {
        credentials: 'same-origin'
    })
        .then(response => response.text())
        .then(html => {
            document.getElementById('person-modal-content').innerHTML = html;
            UIkit.modal('#person-modal').show();
        });
    // No error handling
}
```

**Solution:**
```javascript
function loadPersonModal(personId) {
    fetch(`/graph/person/${personId}`)
        .then(response => {
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return response.text();
        })
        .then(html => {
            document.getElementById('person-modal-content').innerHTML = html;
            UIkit.modal('#person-modal').show();
        })
        .catch(error => {
            console.error('Failed to load person details:', error);
            UIkit.notification({
                message: 'Failed to load person details',
                status: 'danger'
            });
        });
}
```

### Visibility API for Performance

**Problem:** Simulation runs continuously even when tab is hidden.

**Solution:**
```javascript
function setupEventHandlers() {
    // ... existing handlers ...

    // Pause simulation when tab is hidden
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            simulation.stop();
        } else {
            simulation.restart();
        }
    });
}
```

---

## Recommended CONFIG Enhancements

```javascript
const CONFIG = {
    nodeRadius: { min: 8, max: 25 },
    colors: {
        female: '#FF69B4',
        male: '#4169E1',
        unspecified: '#808080'
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
        alphaDecay: 0.0228,  // D3 default, documented for visibility
        velocityDecay: 0.4
    },
    // NEW: Centralize opacity values
    opacity: {
        node: 1,
        link: 0.6,
        dimmedNode: 0.2,
        dimmedLink: 0.1
    },
    // NEW: Centralize link styling
    link: {
        stroke: '#999',
        strokeWidth: 2
    }
};
```

---

## Research Sources

### D3.js Documentation (via Context7)
- [D3 Force Simulation API](https://github.com/d3/d3/blob/main/docs/d3-force/simulation.md)
- [D3 Drag Behavior](https://github.com/d3/d3/blob/main/docs/d3-drag.md)
- [D3 Zoom Behavior](https://github.com/d3/d3/blob/main/docs/d3-zoom.md)
- [D3 Force-Directed Graph Examples](https://context7.com/d3/d3/llms.txt)

### Best Practice Patterns Verified
- Modern event signature `(event, d)` - D3 v6+ pattern
- `fx`/`fy` for drag behavior with spring-back
- `.join()` for data binding
- `d3.zoomIdentity` for zoom reset
- `alphaTarget()` for drag reheating

---

## Summary

| Category | Score | Notes |
|----------|-------|-------|
| Structure | Good | IIFE, CONFIG, separation of concerns |
| D3 Patterns | Good | Modern syntax, correct force/drag/zoom |
| Error Handling | Fair | Missing in modal fetch |
| Performance | Fair | `getNodeRadius` recalculation, no visibility API |
| SVG Correctness | Bug | `<title>` on non-container elements |
| Maintainability | Good | Clear function names, reasonable organization |

---

## Implementation Notes

1. **SVG structure fix is critical** - Tooltips are completely broken
2. **Error handling is quick win** - Add `.catch()` to fetch
3. **Performance fixes** - Cache maxConnections, add visibility API
4. **CONFIG cleanup** - Can be done incrementally

Estimated effort: 1-2 hours for all changes
