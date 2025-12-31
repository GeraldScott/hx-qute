# Qute Fragments Review

Analysis of section 7.4 in `docs/ARCHITECTURE.md` based on deep web research.

**Date:** 2025-12-31
**Status:** Complete (High/Medium priority items done)

---

## Research Sources

| Source Type | Links |
|-------------|-------|
| Official Docs | [Qute Reference Guide](https://quarkus.io/guides/qute-reference) |
| GitHub Issues | [#28753](https://github.com/quarkusio/quarkus/issues/28753), [#29247](https://github.com/quarkusio/quarkus/issues/29247), [#28771](https://github.com/quarkusio/quarkus/issues/28771), [#44281](https://github.com/quarkusio/quarkus/issues/44281), [#6440](https://github.com/quarkusio/quarkus/issues/6440) |
| GitHub PRs | [#28216](https://github.com/quarkusio/quarkus/pull/28216) (original fragment implementation) |
| Discussions | [#41114](https://github.com/quarkusio/quarkus/discussions/41114) |
| HTMX Essays | [Template Fragments](https://htmx.org/essays/template-fragments/) |
| Blog Posts | [Martijn Dashorst](https://martijndashorst.com/blog/2023/08/27/htmx-quarkus-first-impressions), [Gunnar Morling](https://www.morling.dev/blog/quarkus-qute-test-ride/) |

---

## Findings Summary

### 1. Fragment Definition Syntax

**Status:** DONE

Three equivalent syntaxes exist:
```html
{#fragment price}           {!-- bare identifier --}
{#fragment id=price}        {!-- attribute form --}
{#fragment id='price'}      {!-- quoted attribute (recommended) --}
```

The quoted form is recommended for consistency with Qute expression syntax.

**Action Taken:** Updated documentation and all templates to use quoted syntax.

---

### 2. `rendered=false` Attribute

**Status:** ACCURATE

Documentation correctly describes this attribute. Additional options:
- `_hidden` parameter (alternative to `rendered=false`)
- `{#capture name}` alias (implies hidden fragment)

**Known Bug (Fixed):** [GitHub #44281](https://github.com/quarkusio/quarkus/issues/44281) - `rendered=false` was ignored when fragments included other templates containing nested fragments. Fixed in Quarkus 3.15.3.

---

### 3. Including Fragments Within Fragments

**Status:** DONE

**Documentation (section 7.5.4) shows DRY pattern:**
```html
{#fragment id='modal_success' rendered=false}
...
<div id="entity-table-container" hx-swap-oob="innerHTML">
    {#include $table entities=entities /}
</div>
{/fragment}
```

**Action Taken:** Refactored all `modal_success` fragments to use `{#include $table /}`.

---

### 4. Value Resolver Limitation

**Status:** DONE

From official docs:
> "The generated value resolver does not cover the expression used in the included template... Unfortunately, we can't detect the usage in an included template."

**Workarounds:**
1. Add explicit `{@Type param}` declarations in fragments
2. Use `@io.quarkus.qute.TemplateData` annotation

---

### 5. OOB Swaps for Multiple Fragments

**Status:** PARTIALLY DOCUMENTED

[GitHub #29247](https://github.com/quarkusio/quarkus/issues/29247) shows that returning multiple fragments requires workarounds.

**Workaround pattern:**
```java
// oob.html template: {#for i in items}{i.raw}{/for}
public static native TemplateInstance oob(Uni<String>... items);

// Usage:
Templates.oob(
    Templates.notes$noteList(...).createUni(),
    Templates.notes$noteForm(note).createUni()
);
```

Current project embeds OOB elements directly in success fragments (valid approach).

---

### 6. Table Fragment Parameter Declarations

**Status:** DONE

All table fragments now declare their own parameters for self-containment:

| Template | Parameter Declaration |
|----------|----------------------|
| `gender.html` | `{@java.util.List<...Gender> genders}` |
| `title.html` | `{@java.util.List<...Title> titles}` |
| `person.html` | `{@java.util.List<...Person> persons}` + `{@String filterText}` |

**Action Taken:** Added explicit parameter declarations to gender.html and title.html table fragments.

---

## TODO List

### High Priority

- [x] **Refactor success fragments to use `{#include $table /}`**
  - Eliminates ~40 lines of duplicated table markup per template
  - Reduces maintenance burden
  - Matches documented best practice
  - Files: `gender.html`, `title.html`, `person.html`

- [x] **Standardize table fragment parameter declarations**
  - Decide: Should fragments declare their own params or use page-level?
  - Apply consistently across all templates

### Medium Priority

- [x] **Add value resolver limitation note to documentation**
  - Section 7.4 should mention this caveat
  - Include workarounds

- [x] **Document `{#capture}` alias more prominently**
  - Currently only mentioned in key attributes list
  - Consider adding example usage

### Low Priority

- [ ] **Consider CDI producer for HTMX headers**
  - See [Martijn Dashorst's blog](https://martijndashorst.com/blog/2025/02/04/htmx-cdi)
  - Would allow `{#if cdi:htmx.isHtmxRequest}` in templates
  - Alternative to current `@HeaderParam` approach

- [ ] **Evaluate `ignoreFragments` parameter**
  - Syntax: `{#include 'items$price' ignoreFragments=true /}`
  - May be useful for edge cases

---

## Completed Items

- [x] **Standardize table fragment parameter declarations**
  - Added explicit `{@Type param}` declarations to all table fragments
  - Ensures self-contained fragments that work with `{#include /}`
  - Addresses value resolver limitation

- [x] **Add value resolver limitation note to documentation**
  - Added section 7.4 "Value Resolver Limitation"
  - Documents the problem with included fragments
  - Provides solution (explicit declarations) and alternative (@TemplateData)

- [x] **Refactor success fragments to use `{#include $table /}`**
  - Replaced duplicated table HTML with fragment includes
  - gender.html: `{#include $table genders=genders /}`
  - title.html: `{#include $table titles=titles /}`
  - person.html: `{#include $table persons=persons filterText=filterText /}`

- [x] **Use quoted syntax for fragment IDs** (commit c4e6c88)
  - Updated documentation section 7.4
  - Updated all 21 fragments across 3 templates
  - Added syntax options and recommendation

---

## References

### Official Documentation Quotes

> "A fragment represents a part of a template that can be treated as a separate template, i.e. rendered separately. One of the main motivations to introduce this feature was the support of use cases like htmx fragments."

> "Template fragments are a relatively rare Server Side Rendering (SSR) template library feature that allow you to render a fragment or partial bit of the content within a template, rather than the entire template. This feature is very handy in Hypermedia Driven Applications because it allows you to decompose a particular view for partial updates internally without pulling fragments of the template out to separate files."

### Design Philosophy (Martin Kouba, Qute maintainer)

> "It aims to be as simple as possible, but not simpler... In other words, it's always a trade-off between being flexible/powerful and simple/convenient."

> "Both the hx-boost and the fragments have different use cases. It's not one or the other."
