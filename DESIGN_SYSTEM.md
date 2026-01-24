# DESIGN_SYSTEM.md -- Crew Handshake (Token-First, Angular v20+)

## Product Context (fixed decisions)
- Product type: Multi-tenant B2B SaaS (admin + foreman portals) with public worker pages (token links)
- Primary users: Admins (owners/payroll), Foremen (crew leads), Workers (W2/1099 via link)
- Usage pattern:
  - Workers: short sessions, mobile-first, low attention, low tolerance for friction
  - Foremen: frequent daily use, mobile-first, list-heavy, needs speed
  - Admins: weekly/daily ops, desktop-first, dense data (tables/forms)
- Brand personality: Calm, trustworthy, modern, neutral (no construction cosplay, no decorative noise)
- Business goal: Reduce friction and disputes; build trust through clarity and predictable records

---

## 1) Design Principles (System-Level)

1. One primary action per state
   Each screen state exposes one obvious next step action; secondary actions are visually subdued.

2. Tokens are the single source of truth
   Colors, spacing, typography, radius, and shadows come only from tokens; no ad-hoc values in components.

3. Neutral by default, status by exception
   The UI is mostly neutral; color is reserved for actions, focus, and status states to reduce cognitive load.

4. Accessible interaction is not optional
   Every interactive element must be keyboard reachable, show a visible focus ring, and meet WCAG AA contrast.

5. Dense information, calm presentation
   Use consistent hierarchy, spacing, and dividers to support scanning without visual clutter.

6. Trust-first visuals
   Avoid UI patterns that feel like monitoring; prefer receipt/confirmation patterns and transparent change indicators.

---

## 2) Color System (Token-First)

### Color usage rules (global)
- Text must meet WCAG AA contrast against its background (minimum 4.5:1 for normal text).
- Status colors are used for states, not decoration.
- Primary color is used for primary actions and links, not for backgrounds of whole pages.
- Accent is used sparingly for small highlights, focus reinforcement, and informational emphasis.

### Semantic color tokens (CSS variables)
All colors below are semantic tokens. Components use semantic tokens only.

#### Primary (actions and links)
- `--ds-color-primary` `#2563EB`
  - Usage: primary button background, primary links, active states
  - Do not use for: warning/success/error messaging; page backgrounds
  - Contrast notes: use `--ds-color-text-inverse` on primary for AA

- `--ds-color-primary-hover` `#1D4ED8`
  - Usage: hover state for primary buttons/links
  - Do not use for: body text

- `--ds-color-primary-subtle` `#EFF6FF`
  - Usage: subtle highlight backgrounds (selected row, info panels)
  - Do not use for: primary button background (too low contrast)

#### Secondary (neutral emphasis)
- `--ds-color-secondary` `#334155`
  - Usage: secondary action text/icon emphasis, neutral active chips
  - Do not use for: long-form body text (use text tokens)

#### Accent (small emphasis)
- `--ds-color-accent` `#0D9488`
  - Usage: small highlights (badges, toggles, subtle emphasis)
  - Do not use for: primary CTA buttons; error/success; body text paragraphs
  - Contrast notes: accent is not guaranteed AA on white for small text; use for icons, borders, small chips only

#### Backgrounds and surfaces
- `--ds-color-bg` `#FFFFFF`
  - Usage: app base background
  - Do not use for: elevated surfaces that need separation without borders

- `--ds-color-bg-subtle` `#F8FAFC`
  - Usage: page sections, table headers, subtle panels
  - Do not use for: primary cards that need strong separation (use surface tokens + border/shadow)

- `--ds-color-surface` `#FFFFFF`
  - Usage: cards, modals, panels (default surface)
  - Do not use for: page background if you need visual grouping (use bg/bg-subtle)

- `--ds-color-surface-elevated` `#FFFFFF`
  - Usage: surfaces that use shadow token to separate from bg
  - Do not use for: nested cards; nested elevation creates noise

- `--ds-color-surface-inverse` `#0F172A`
  - Usage: inverse surfaces (rare): dark header strips, inverse badges
  - Do not use for: entire page themes in MVP

#### Text
- `--ds-color-text` `#0F172A`
  - Usage: primary text
  - Do not use for: disabled text (use muted/disabled)

- `--ds-color-text-secondary` `#334155`
  - Usage: secondary labels, descriptions, metadata
  - Do not use for: long dense tables where primary readability is required

- `--ds-color-text-muted` `#64748B`
  - Usage: placeholders, helper text, low-priority metadata
  - Do not use for: key information or interactive labels

- `--ds-color-text-inverse` `#FFFFFF`
  - Usage: text on primary/destructive/inverse surfaces
  - Do not use for: text on light surfaces

- `--ds-color-text-disabled` `#94A3B8`
  - Usage: disabled labels and values
  - Do not use for: body text; must remain clearly disabled

#### Borders and dividers
- `--ds-color-border` `#E2E8F0`
  - Usage: default borders (inputs, cards, dividers)
  - Do not use for: focus indication (use focus ring)

- `--ds-color-border-strong` `#CBD5E1`
  - Usage: stronger separators (table outer border, section separators)
  - Do not use for: subtle dividers (use default border)

#### Focus ring (accessibility)
- `--ds-color-focus-ring` `#93C5FD`
  - Usage: focus ring box-shadow color
  - Do not use for: status messaging

#### Status colors (semantic)
Status colors always have a bg + border + text pairing to maintain readability.

Success:
- `--ds-color-success` `#16A34A` (for icons/strong accents)
- `--ds-color-success-bg` `#DCFCE7` (background)
- `--ds-color-success-border` `#86EFAC` (border)
- `--ds-color-success-text` `#166534` (text on success-bg)

Warning:
- `--ds-color-warning` `#D97706`
- `--ds-color-warning-bg` `#FFEDD5`
- `--ds-color-warning-border` `#FDBA74`
- `--ds-color-warning-text` `#9A3412`

Error:
- `--ds-color-error` `#DC2626`
- `--ds-color-error-bg` `#FEE2E2`
- `--ds-color-error-border` `#FCA5A5`
- `--ds-color-error-text` `#991B1B`

Info:
- `--ds-color-info` `#0284C7`
- `--ds-color-info-bg` `#E0F2FE`
- `--ds-color-info-border` `#7DD3FC`
- `--ds-color-info-text` `#075985`

Status do not use rules:
- Do not use warning/error colors for primary navigation or primary actions.
- Do not place `--ds-color-text-muted` on status backgrounds; use the status text tokens.

---

## 3) Typography System (Angular-Friendly)

### Font families
- `--ds-font-family-sans`:
  `ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, Arial, "Noto Sans", "Liberation Sans", sans-serif`
- `--ds-font-family-mono`:
  `ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace`

Decision: system font stack for reliability, speed, and consistent rendering across devices (especially field mobile).

### Type scale (dashboard + forms optimized)
All sizes are fixed tokens. Do not invent sizes.

- `--ds-font-size-h1`: `1.5rem` (24px)
  `--ds-line-height-h1`: `2rem` (32px)
  Usage: admin page title only

- `--ds-font-size-h2`: `1.25rem` (20px)
  `--ds-line-height-h2`: `1.75rem` (28px)
  Usage: section titles, foreman page title

- `--ds-font-size-h3`: `1.125rem` (18px)
  `--ds-line-height-h3`: `1.625rem` (26px)
  Usage: card titles, key panels

- `--ds-font-size-h4`: `1rem` (16px)
  `--ds-line-height-h4`: `1.5rem` (24px)
  Usage: dense section headers, modal titles on mobile

- `--ds-font-size-body`: `0.875rem` (14px)
  `--ds-line-height-body`: `1.25rem` (20px)
  Usage: default UI text (forms, lists, table cells)

- `--ds-font-size-small`: `0.75rem` (12px)
  `--ds-line-height-small`: `1rem` (16px)
  Usage: helper text, meta labels, secondary captions

- `--ds-font-size-caption`: `0.6875rem` (11px)
  `--ds-line-height-caption`: `1rem` (16px)
  Usage: overlines, timestamps in dense lists (sparingly)

### Font weights
- `--ds-font-weight-regular`: `400`
- `--ds-font-weight-medium`: `500`
- `--ds-font-weight-semibold`: `600`

Usage rules:
- Body text uses 400.
- Form labels use 500.
- Titles use 600.
- Do not use bold (700+) in MVP to avoid visual noise and inconsistent rendering.

---

## 4) Spacing and Sizing System

### Base unit
- Base spacing unit is 4px.

### Spacing scale (only allowed values)
- `--ds-space-0`: `0px`
- `--ds-space-1`: `4px`
- `--ds-space-2`: `8px`
- `--ds-space-3`: `12px`
- `--ds-space-4`: `16px`
- `--ds-space-5`: `20px`
- `--ds-space-6`: `24px`
- `--ds-space-8`: `32px`
- `--ds-space-10`: `40px`
- `--ds-space-12`: `48px`
- `--ds-space-16`: `64px`

### Padding vs margin rules (enforced)
- Use padding inside components; use margin only between components.
- Page-level layouts use gap (grid-gap/flex gap) instead of manual margins whenever possible.
- Do not stack multiple margins between the same elements; use a single parent gap.

### Component internal spacing guidance
- Buttons: horizontal padding = `--ds-space-4` (16px) for md, `--ds-space-3` (12px) for sm
- Inputs: horizontal padding = `--ds-space-3` (12px)
- Cards: padding = `--ds-space-4` (16px)
- Modals: padding = `--ds-space-6` (24px)
- List items: vertical padding = `--ds-space-3` (12px), horizontal = `--ds-space-4` (16px)

### Minimum touch targets (accessibility)
- Minimum interactive height: 40px
- Minimum icon button size: 40x40px

---

## 5) Component Foundations (Rules, Not One-Offs)

### Global component rules
- All interactive components must have:
  - visible hover and focus states
  - disabled state with non-interactive cursor
  - accessible name (text or `aria-label`)
- Focus ring is mandatory:
  - `box-shadow: 0 0 0 3px var(--ds-color-focus-ring);`
  - never remove focus outlines

### Border radius (single source of truth)
- `--ds-radius`: `12px` (default for all components)
- `--ds-radius-round`: `9999px` (only for pills/circular controls)

Rule: every component uses `--ds-radius` unless it is explicitly pill/circle UI.

### Elevation / shadow scale (minimal)
- `--ds-shadow-1`: `0 1px 2px rgba(15, 23, 42, 0.06)`
- `--ds-shadow-2`: `0 6px 18px rgba(15, 23, 42, 0.10)`

Rule: Cards use border by default; use shadow only for modals and key elevated surfaces.

### 5.1 Buttons (foundation rules)
Button types: primary, secondary, destructive, ghost

Shared button rules:
- Height: 40px (default)
- Horizontal padding: 16px
- Border radius: `--ds-radius`
- Font: body (14px) weight 600 for primary, 500 for others
- Focus ring: required
- Disabled: reduce opacity and remove hover changes; cursor not-allowed

Primary button:
- Background: `--ds-color-primary`
- Text: `--ds-color-text-inverse`
- Hover: `--ds-color-primary-hover`
- Do not use primary style for cancel or secondary actions.

Secondary button:
- Background: transparent
- Border: 1px solid `--ds-color-border`
- Text: `--ds-color-text`
- Hover: background `--ds-color-bg-subtle`
- Use for secondary confirm actions.

Destructive button:
- Background: `--ds-color-error`
- Text: `--ds-color-text-inverse`
- Hover: darken using the same token approach (do not invent a new red; use opacity overlay or define a single hover token if needed)
- Use only for irreversible actions (delete, remove).

Ghost button:
- Background: transparent
- Text: `--ds-color-text-secondary`
- Hover: background `--ds-color-bg-subtle`
- Use for tertiary actions in dense UIs.

Accessibility notes:
- Icon-only buttons must include `aria-label`.
- Disabled buttons must remain readable (do not drop below legible contrast).

### 5.2 Inputs and form fields (foundation rules)
Shared input rules:
- Height: 40px
- Padding: 12px horizontal
- Background: `--ds-color-surface`
- Border: 1px solid `--ds-color-border`
- Text: `--ds-color-text`
- Placeholder: `--ds-color-text-muted`
- Border radius: `--ds-radius`

Focus state:
- Border color stays neutral; focus indicated by focus ring token.
- Do not use border-only focus; it is not reliably visible.

Error state:
- Border: `--ds-color-error`
- Helper text: `--ds-color-error-text`
- Show error text below field with `--ds-font-size-small`

Label rules:
- Labels are always visible (no placeholder-as-label).
- Label uses `--ds-font-weight-medium`, `--ds-color-text-secondary`.

Disabled state:
- Background: `--ds-color-bg-subtle`
- Text: `--ds-color-text-disabled`
- Cursor: not-allowed

Accessibility notes:
- Every input must have a programmatic label.
- Error text must be associated with input via `aria-describedby`.

### 5.3 Cards and surfaces (foundation rules)
Card rules:
- Background: `--ds-color-surface`
- Border: 1px solid `--ds-color-border`
- Radius: `--ds-radius`
- Padding: `--ds-space-4`
- Shadow: none by default

Elevated surfaces (rare):
- Same as card + `--ds-shadow-1`
- Use only when you need separation from a similarly colored background.

### 5.4 Modals / sheets (foundation rules)
Modal rules:
- Overlay: `rgba(15, 23, 42, 0.45)`
- Surface: `--ds-color-surface-elevated` + `--ds-shadow-2`
- Radius: `--ds-radius`
- Padding: `--ds-space-6`
- Close button must be keyboard accessible and labeled.

Behavior rules:
- Focus is trapped inside the modal.
- Escape closes modal unless doing so would lose unsaved changes (then confirm dialog is required).
- Background scrolling is prevented when modal is open.

Sheets (mobile bottom sheets):
- Use when the primary user is on mobile and the action is short.
- Same overlay rules; surface anchored bottom with radius on top corners using `--ds-radius`.

### 5.5 Tables and lists (foundation rules)
Lists (default for foreman mobile views):
- Row height minimum 44px
- Divider: 1px solid `--ds-color-border`
- Hover (desktop): background `--ds-color-bg-subtle`
- Selected: background `--ds-color-primary-subtle` with left border `--ds-color-primary`

Tables (admin desktop views):
- Header background: `--ds-color-bg-subtle`
- Header text: `--ds-color-text-secondary` weight 600
- Cell padding: `--ds-space-3` vertical, `--ds-space-4` horizontal
- Border: 1px solid `--ds-color-border` outer; row dividers inside
- Empty state row must be explicit (not blank)

Accessibility notes:
- Clickable rows must be keyboard navigable.
- Do not rely on color alone to show status; include text label or icon with accessible name.

---

## 6) Layout and Grid System

### Max content widths
- `--ds-layout-max-width-admin`: `1200px`
- `--ds-layout-max-width-foreman`: `720px`
- `--ds-layout-max-width-worker`: `480px`

Rules:
- Worker pages are centered with max width 480px on larger screens.
- Foreman pages are mobile-first; max width 720px avoids overly wide lists.
- Admin pages allow dense layouts up to 1200px.

### Grid columns
- Desktop grid: 12 columns
- Mobile layout: single column (do not force multi-column on mobile)

### Page padding (responsive)
- Mobile: 16px
- Tablet and up: 24px
- Large desktop: 32px

### Breakpoints (authoritative)
- `--ds-bp-sm`: `480px`
- `--ds-bp-md`: `768px`
- `--ds-bp-lg`: `1024px`
- `--ds-bp-xl`: `1280px`

Layout rules:
- Do not add new breakpoints.
- Use these breakpoints for major layout changes only.

---

## 7) Design Tokens (Implementation-Ready)

Copy-paste this into a global stylesheet (e.g., `frontend/src/styles/tokens.css`) and import once.

```css
:root {
  /* Typography */
  --ds-font-family-sans: ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, Arial, "Noto Sans", "Liberation Sans", sans-serif;
  --ds-font-family-mono: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;

  --ds-font-weight-regular: 400;
  --ds-font-weight-medium: 500;
  --ds-font-weight-semibold: 600;

  --ds-font-size-h1: 1.5rem;       /* 24px */
  --ds-line-height-h1: 2rem;       /* 32px */
  --ds-font-size-h2: 1.25rem;      /* 20px */
  --ds-line-height-h2: 1.75rem;    /* 28px */
  --ds-font-size-h3: 1.125rem;     /* 18px */
  --ds-line-height-h3: 1.625rem;   /* 26px */
  --ds-font-size-h4: 1rem;         /* 16px */
  --ds-line-height-h4: 1.5rem;     /* 24px */
  --ds-font-size-body: 0.875rem;   /* 14px */
  --ds-line-height-body: 1.25rem;  /* 20px */
  --ds-font-size-small: 0.75rem;   /* 12px */
  --ds-line-height-small: 1rem;    /* 16px */
  --ds-font-size-caption: 0.6875rem; /* 11px */
  --ds-line-height-caption: 1rem;    /* 16px */

  /* Spacing (4px base) */
  --ds-space-0: 0px;
  --ds-space-1: 4px;
  --ds-space-2: 8px;
  --ds-space-3: 12px;
  --ds-space-4: 16px;
  --ds-space-5: 20px;
  --ds-space-6: 24px;
  --ds-space-8: 32px;
  --ds-space-10: 40px;
  --ds-space-12: 48px;
  --ds-space-16: 64px;

  /* Radius */
  --ds-radius: 12px;
  --ds-radius-round: 9999px;

  /* Shadows */
  --ds-shadow-1: 0 1px 2px rgba(15, 23, 42, 0.06);
  --ds-shadow-2: 0 6px 18px rgba(15, 23, 42, 0.10);

  /* Core colors */
  --ds-color-primary: #2563EB;
  --ds-color-primary-hover: #1D4ED8;
  --ds-color-primary-subtle: #EFF6FF;

  --ds-color-secondary: #334155;
  --ds-color-accent: #0D9488;

  --ds-color-bg: #FFFFFF;
  --ds-color-bg-subtle: #F8FAFC;

  --ds-color-surface: #FFFFFF;
  --ds-color-surface-elevated: #FFFFFF;
  --ds-color-surface-inverse: #0F172A;

  --ds-color-text: #0F172A;
  --ds-color-text-secondary: #334155;
  --ds-color-text-muted: #64748B;
  --ds-color-text-inverse: #FFFFFF;
  --ds-color-text-disabled: #94A3B8;

  --ds-color-border: #E2E8F0;
  --ds-color-border-strong: #CBD5E1;

  --ds-color-focus-ring: #93C5FD;

  /* Status colors */
  --ds-color-success: #16A34A;
  --ds-color-success-bg: #DCFCE7;
  --ds-color-success-border: #86EFAC;
  --ds-color-success-text: #166534;

  --ds-color-warning: #D97706;
  --ds-color-warning-bg: #FFEDD5;
  --ds-color-warning-border: #FDBA74;
  --ds-color-warning-text: #9A3412;

  --ds-color-error: #DC2626;
  --ds-color-error-bg: #FEE2E2;
  --ds-color-error-border: #FCA5A5;
  --ds-color-error-text: #991B1B;

  --ds-color-info: #0284C7;
  --ds-color-info-bg: #E0F2FE;
  --ds-color-info-border: #7DD3FC;
  --ds-color-info-text: #075985;

  /* Layout tokens */
  --ds-layout-max-width-admin: 1200px;
  --ds-layout-max-width-foreman: 720px;
  --ds-layout-max-width-worker: 480px;

  --ds-layout-page-padding: 16px; /* overridden at breakpoints */

  /* Breakpoints */
  --ds-bp-sm: 480px;
  --ds-bp-md: 768px;
  --ds-bp-lg: 1024px;
  --ds-bp-xl: 1280px;
}

@media (min-width: 768px) {
  :root { --ds-layout-page-padding: 24px; }
}

@media (min-width: 1280px) {
  :root { --ds-layout-page-padding: 32px; }
}
```

---

## 8) Do / Dont Rules (Critical Guardrails)

### Do

- Do use semantic tokens only (`--ds-color-*`, `--ds-space-*`, `--ds-font-*`).
- Do use one primary action style per view/state (primary button appears once per state).
- Do show focus rings on all interactive elements.
- Do use status palettes only for status messaging and status indicators.
- Do keep layouts calm: prefer dividers, spacing, and typography over color blocks.

### Dont

- Dont introduce new hex colors, spacing values, font sizes, or radii in components.
- Dont remove focus outlines or replace them with subtle borders.
- Dont use color alone to convey meaning (status requires text/icon labels).
- Dont invent new button styles (tertiary, outline-primary, soft-primary) outside the four defined button types.
- Dont nest elevated cards within elevated cards; it creates visual drift.
- Dont use placeholder text as the only label for an input.
- Dont create icon-only actions without an accessible name (`aria-label`).
- Dont add new breakpoints or custom layout widths per page.
- Dont add decorative gradients, textures, or construction-themed visuals; trust is built by clarity, not decoration.
