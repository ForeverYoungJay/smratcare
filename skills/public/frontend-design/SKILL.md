---
name: frontend-design
description: Create distinctive, production-grade frontend interfaces with strong visual identity and polished implementation details. Use when Codex needs to design or build a web component, full page, or frontend application with intentional aesthetic direction, refined typography, cohesive color systems, non-generic layouts, and production-ready HTML/CSS/JS/React/Vue code.
---

# Frontend Design

## Overview

Design and implement frontend experiences that feel intentionally art-directed rather than template-generated.
Commit to a clear aesthetic point of view, then execute it with production-grade code quality.

## Workflow

### 1) Define the design intent before coding

Establish the following in 3-6 crisp bullets before writing code:

- Purpose: identify the user, the job-to-be-done, and the primary interaction outcome.
- Tone: choose a distinct style direction and commit to it (for example: editorial, retro-futurist, brutalist, playful, luxury, industrial, organic, soft).
- Constraints: capture framework, performance, accessibility, browser, and responsiveness requirements.
- Differentiator: define one memorable visual or interaction signature that makes the interface recognizable.

Keep this direction explicit and let it guide every implementation decision.

### 2) Build a coherent visual system

Use CSS custom properties for tokens and keep the system cohesive:

- Define a dominant palette with intentional accent contrast.
- Pair a characterful display typeface with a readable body typeface.
- Establish spacing, radius, border, and shadow scales that match the chosen tone.
- Create layered backgrounds and atmospheric details (texture, mesh, pattern, transparency, grain, geometry) when stylistically appropriate.

Avoid generic defaults and avoid drifting into mixed, inconsistent styles.

### 3) Compose layouts with intent

Construct layouts that reinforce the concept rather than defaulting to standard card grids:

- Use asymmetry, overlap, diagonal rhythm, density shifts, or negative space with intention.
- Keep hierarchy obvious through type scale, contrast, and motion pacing.
- Preserve usability: clear focus states, logical tab order, and readable content flow.
- Ensure responsive behavior remains elegant, not merely stacked.

### 4) Add meaningful motion

Implement motion that supports narrative and affordance:

- Prioritize a strong first impression with orchestrated load-in sequencing.
- Use hover/focus/press transitions to communicate interactivity.
- Prefer CSS-based animation where possible; use framework motion libs only when needed.
- Keep timing and easing consistent with the aesthetic direction.

Favor a few high-impact motion moments over noisy, scattered effects.

### 5) Deliver production-grade implementation

Produce working code that is maintainable and complete:

- Implement real interactions, not static mockups.
- Ensure responsive behavior across standard breakpoints.
- Include semantic structure and accessibility essentials (landmarks, labels, contrast, keyboard states).
- Keep code organized, reusable, and aligned with the project stack.

## Quality Bar

Meet all of the following:

- Clear and bold design direction is visible in the final output.
- Typography, color, spacing, and motion feel systematized and cohesive.
- Interface avoids overused "AI-generic" patterns and visual clichés.
- Feature implementation is functional and ready for integration.
- Details feel intentionally refined (micro-interactions, alignment, rhythm, balance).

## Creative Direction Starters

When the user provides minimal style input, propose one focused direction and proceed:

- Editorial Contrast: dramatic headlines, grid breaks, high-contrast rhythm.
- Neo-Brutalist Utility: rigid geometry, heavy borders, deliberate rawness.
- Soft Atmospheric: muted palette, gentle gradients, tactile spacing.
- Luxe Minimal: restrained palette, premium typography, subtle depth.
- Playful Kinetic: expressive color, animated motifs, toy-like feedback.

Use exactly one primary direction unless the user explicitly asks for fusion.

## Anti-Patterns to Avoid

Never ship designs that rely on:

- overused generic font stacks,
- predictable white-background + purple-gradient defaults,
- cookie-cutter dashboard blocks without concept,
- disconnected effects that do not support hierarchy,
- visual density that ignores readability and interaction clarity.

## Output Expectations

When asked to build UI with this skill:

- State the chosen aesthetic direction in one concise sentence.
- Implement complete, runnable frontend code in the requested stack.
- Include deliberate decisions in typography, palette, layout, and motion.
- Ensure implementation quality is suitable for production iteration.
