# Navbar Accessibility Implementation Tasks

**Date:** December 31, 2024
**Status:** Pending Implementation
**Source:** Navbar Component Review Document
**Files:** `base.html`, `style.css`

---

## Comparison Summary

The review document identifies 7 accessibility issues plus additional CSS enhancements. After comparing against the current implementation, **none of the recommendations have been implemented yet**.

| Issue | Severity | Status |
|-------|----------|--------|
| Sidebar uses `<aside>` instead of `<nav>` | High | Not Implemented |
| Missing `<main>` landmark | High | Not Implemented |
| No skip link | High | Not Implemented |
| Navigation regions lack unique labels | Medium | Not Implemented |
| Mobile header uses `<div>` instead of `<header>` | Medium | Not Implemented |
| Close button missing accessible label | Low | Not Implemented |
| Menu toggle missing accessible label | Low | Not Implemented |
| Skip link CSS styles | High | Not Implemented |
| Focus visibility enhancements | Medium | Not Implemented |
| Reduced motion preferences | Low | Not Implemented |

---

## Todo List

### Priority 1: High Severity (WCAG Level A)

- [ ] **Change sidebar from `<aside>` to `<nav>`**
  - File: `base.html:48`
  - Change: `<aside class="sidebar uk-height-viewport">` to `<nav class="sidebar uk-height-viewport" aria-label="Main">`
  - Also update closing tag at line 159

- [ ] **Change main content wrapper to `<main>`**
  - File: `base.html:163`
  - Change: `<div class="uk-width-expand@l main-content-area">` to `<main class="uk-width-expand@l main-content-area">`
  - Also update closing tag at line 169

- [ ] **Add skip link as first focusable element**
  - File: `base.html:30` (after `<body>`, before offcanvas-content div)
  - Add: `<a href="#main-content" class="skip-link">Skip to main content</a>`
  - Note: Target `#main-content` already exists at line 165

- [ ] **Add skip link CSS styles**
  - File: `style.css`
  - Add `--focus-color` CSS variable to `:root`
  - Add `.skip-link` styles with hidden-until-focused behavior

### Priority 2: Medium Severity

- [ ] **Add aria-labels to navigation regions**
  - Desktop nav (line 48): `aria-label="Main"`
  - Mobile offcanvas nav (line 174): Change `<div class="uk-offcanvas-bar sidebar">` to `<nav class="uk-offcanvas-bar sidebar" aria-label="Mobile menu">`
  - Update closing tag at line 275

- [ ] **Change mobile header from `<div>` to `<header>`**
  - File: `base.html:32`
  - Change: `<div class="uk-navbar-container uk-hidden@l" uk-navbar>` to `<header class="uk-navbar-container uk-hidden@l" uk-navbar>`
  - Update closing tag at line 42

- [ ] **Add enhanced focus-visible styles**
  - File: `style.css`
  - Add `:focus-visible` rules for interactive elements

### Priority 3: Low Severity

- [ ] **Add aria-label to offcanvas close button**
  - File: `base.html:175-179`
  - Change: `<button class="uk-offcanvas-close" type="button" uk-close></button>`
  - To: `<button class="uk-offcanvas-close" type="button" uk-close aria-label="Close menu"></button>`

- [ ] **Add aria-label to mobile menu toggle**
  - File: `base.html:34-39`
  - Add `aria-label="Open menu"` to the navbar toggle anchor

- [ ] **Add reduced motion media query**
  - File: `style.css`
  - Add `@media (prefers-reduced-motion: reduce)` to disable transitions

---

## Implementation Details

### Skip Link CSS (Required)

```css
/* Add to :root */
--focus-color: #1e87f0;

/* Skip link styles */
.skip-link {
    position: absolute;
    top: -50px;
    left: 0;
    background: var(--brand-green);
    color: #fff;
    padding: 12px 24px;
    z-index: 10000;
    text-decoration: none;
    font-weight: 600;
    transition: top 0.2s ease-in-out;
}

.skip-link:focus {
    top: 0;
    outline: 3px solid var(--focus-color);
    outline-offset: 2px;
}
```

### Focus Visibility CSS (Recommended)

```css
a:focus-visible,
button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible,
[tabindex]:focus-visible {
    outline: 2px solid var(--focus-color);
    outline-offset: 2px;
}
```

### Reduced Motion CSS (Recommended)

```css
@media (prefers-reduced-motion: reduce) {
    .skip-link,
    .feature-card,
    .logo-link {
        transition: none;
    }
}
```

---

## Testing Checklist

After implementation, verify:

- [ ] Tab through page - skip link appears on first Tab press
- [ ] Activate skip link - focus moves to main content
- [ ] Screen reader announces correct landmarks (navigation, main)
- [ ] Multiple navigations are distinguishable by label
- [ ] All interactive elements have visible focus indicators
- [ ] Focus order follows logical reading order
- [ ] Animations respect reduced motion preference

---

## WCAG References

| Guideline | Level | Issue |
|-----------|-------|-------|
| 1.3.1 Info and Relationships | A | Semantic elements, landmarks |
| 2.4.1 Bypass Blocks | A | Skip link |
| 2.4.7 Focus Visible | AA | Focus indicators |
| 4.1.2 Name, Role, Value | A | Accessible labels |
| 2.3.3 Animation from Interactions | AAA | Reduced motion |
