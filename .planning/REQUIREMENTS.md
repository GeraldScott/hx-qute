# Requirements: HX-Qute HTMX Cleanup

**Defined:** 2026-02-19
**Core Value:** Clean, copy-worthy reference patterns — no dead code

## v1 Requirements

### Dead Code Removal

- [ ] **CLEAN-01**: GenderResource.list() always returns full page (remove HX-Request branching)
- [ ] **CLEAN-02**: TitleResource.list() always returns full page (remove HX-Request branching)
- [ ] **CLEAN-03**: RelationshipResource.list() always returns full page (remove HX-Request branching)
- [ ] **CLEAN-04**: PersonResource.list() always returns full page (remove HX-Request branching)
- [ ] **CLEAN-05**: PersonRelationshipResource list endpoint always returns full page (remove HX-Request branching)
- [ ] **CLEAN-06**: GraphResource list endpoint always returns full page (remove HX-Request branching)

### Template Cleanup

- [ ] **TMPL-01**: Remove any orphaned template files that only existed to support the HX-Request branching pattern

### Documentation

- [ ] **DOCS-01**: Update docs/ARCHITECTURE.md to remove the HX-Request pattern from the Resource Layer section

## v2 Requirements

### HTMX Navigation Enhancement

- **NAV-01**: Add hx-boost to sidebar navigation for AJAX page transitions
- **NAV-02**: Add hx-select for client-side content extraction
- **NAV-03**: Handle inheritance poisoning for modal CRUD operations

## Out of Scope

| Feature | Reason |
|---------|--------|
| Adding test coverage | Separate milestone |
| hx-boost navigation | Chosen as v2 enhancement, not part of current cleanup |
| New features or entities | This is a cleanup-only milestone |
| CSS or UIkit changes | No frontend framework changes |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| CLEAN-01 | Phase 1 | Pending |
| CLEAN-02 | Phase 1 | Pending |
| CLEAN-03 | Phase 1 | Pending |
| CLEAN-04 | Phase 1 | Pending |
| CLEAN-05 | Phase 1 | Pending |
| CLEAN-06 | Phase 1 | Pending |
| TMPL-01 | Phase 1 | Pending |
| DOCS-01 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 8 total
- Mapped to phases: 8
- Unmapped: 0 ✓

---
*Requirements defined: 2026-02-19*
*Last updated: 2026-02-19 after initial definition*
