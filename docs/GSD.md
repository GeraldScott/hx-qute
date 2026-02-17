# GSD (Get Stuff Done) Framework

This document describes the GSD framework, how it relates to this project, and why we use it selectively alongside our primary spec-driven workflow.

## What GSD Is

GSD is an open-source meta-prompting and context engineering system for Claude Code. Created by TACHES and available at [github.com/gsd-build/get-shit-done](https://github.com/gsd-build/get-shit-done), it provides a structured lifecycle for AI-assisted development: interview, research, plan, execute, verify.

Its core innovation is **combating context rot** -- the quality degradation that occurs as Claude's context window fills up during long coding sessions. GSD solves this by spawning fresh subagent instances (each with a clean 200k-token context) for individual tasks, so quality remains consistent regardless of session length.

As of February 2026, GSD has ~8,500+ GitHub stars and is the most widely adopted Claude Code workflow framework, with ports to OpenCode and Gemini CLI.

## How GSD Works (Full Pipeline)

The complete GSD pipeline follows a phased loop:

```
/gsd:new-project       -- Interview: define goals, constraints, stack
/gsd:map-codebase      -- Analyze: parallel agents scan architecture, conventions, concerns
/gsd:discuss-phase N   -- Clarify: interactive Q&A before planning
/gsd:plan-phase N      -- Plan: create atomic task plans (PLAN.md files)
/gsd:execute-phase N   -- Execute: subagents implement tasks in parallel waves
/gsd:verify-work N     -- Verify: goal-backward validation of results
/gsd:complete-milestone -- Archive: tag release and prepare for next cycle
```

Key architectural patterns:
- **Fresh subagent contexts**: Each task runs in a new Claude instance, preventing quality degradation
- **Plans as prompts**: PLAN.md files ARE the executable instructions that subagents read directly
- **Wave-based parallelism**: Independent tasks run simultaneously; dependent tasks wait
- **Atomic commits**: Each task gets its own revertable git commit
- **Session continuity**: STATE.md tracks progress across sessions

## How GSD Is Used in This Project

**We use GSD as a utility belt, not as the primary workflow orchestrator.**

This project has an established spec-driven workflow (`docs/DEVELOPMENT-WORKFLOW.md`) with custom slash commands (`/implement-feature`, `/validate-feature`), per-feature spec files, and an E2E test runner subagent. That system is more tightly integrated with our Quarkus/HTMX/Qute stack and operates at a finer granularity (use-case level) than GSD's phase-based approach.

### What We Use

| Command | Purpose |
|---------|---------|
| `/gsd:quick` | Ad-hoc tasks and bug fixes with atomic commits |
| `/gsd:map-codebase` | Periodic refresh of codebase analysis in `.planning/codebase/` |
| `/gsd:pause-work` | Save session context when stopping mid-task |
| `/gsd:resume-work` | Restore context in a new session |
| `/gsd:debug` | Systematic debugging with persistent state |
| `/gsd:add-todo` | Capture ideas from conversation context |
| `/gsd:check-todos` | Review pending items |

### What We Don't Use

The full discuss -> plan -> execute -> verify pipeline for feature implementation. Our `specs/` workflow handles this with:
- `specs/NNN-feature-name/use-cases.md` (requirements)
- `specs/NNN-feature-name/spec.md` (technical design)
- `specs/NNN-feature-name/tasks.md` (progress tracking)
- `specs/NNN-feature-name/test-cases.md` (E2E verification)
- `specs/PROJECT-PLAN.md` (single source of truth)

### Artifacts

GSD maintains these files:

| Path | Purpose | Status |
|------|---------|--------|
| `.planning/codebase/` | 7 analysis docs (stack, architecture, conventions, etc.) | Active -- refreshed periodically |
| `.planning/config.json` | GSD settings (mode, depth, model profile) | Active |
| `.claude/commands/gsd/` | Slash command definitions | Active |
| `.claude/get-shit-done/` | Framework internals (workflows, templates, references) | Active |

Planning artifacts (PROJECT.md, ROADMAP.md, STATE.md, REQUIREMENTS.md, phases/) have been removed as they duplicated our `specs/` workflow tracking.

## Why This Approach

### Benefits of selective GSD usage

1. **Context rot mitigation** -- `/gsd:quick` spawns a fresh subagent for one-off tasks, keeping the main session clean
2. **Codebase awareness** -- `/gsd:map-codebase` produces structured analysis that helps Claude understand project conventions
3. **Session continuity** -- Pause/resume commands preserve context across sessions without manual note-taking

### Why not the full pipeline

1. **Redundant with specs/ workflow** -- Our spec-driven system already provides structured requirements, technical design, task tracking, and E2E testing
2. **Granularity mismatch** -- GSD operates at "phase" level; our workflow operates at "use case" level, which is more precise
3. **Stack integration** -- Our custom skills (htmx-patterns, java-patterns, postgresql-java) and the e2e-test-runner subagent are deeply integrated with our Quarkus/HTMX stack; GSD's generic execution pipeline doesn't leverage these
4. **Single source of truth** -- Running two tracking systems (specs/PROJECT-PLAN.md + .planning/ROADMAP.md) creates confusion about what's actually done

## Known Criticisms of GSD and SDD Frameworks

For context, these are the broader criticisms of GSD and spec-driven development frameworks as of early 2026:

- **Waterfall revival** -- Heavy spec-before-code approaches risk recreating Big Design Up Front problems that agile methods were designed to solve
- **Context rot is a temporary problem** -- As LLM context windows grow and models improve at using them, the core problem GSD solves may become irrelevant
- **Overkill for small tasks** -- Running a bug fix through discuss -> plan -> execute -> verify is disproportionate overhead
- **Spec drift** -- As the codebase evolves, keeping planning artifacts in sync with code reality creates a maintenance burden
- **Fragile orchestration** -- Subagent spawning failures can break the entire workflow
- **Diminishing returns on mature projects** -- SDD frameworks shine for greenfield; for brownfield projects with established patterns, they add overhead without proportional benefit

These criticisms informed our decision to use GSD selectively rather than as the primary workflow.

## Sources

- [GitHub -- gsd-build/get-shit-done](https://github.com/gsd-build/get-shit-done) -- Official repository
- [The New Stack -- Beating context rot in Claude Code with GSD](https://thenewstack.io/beating-the-rot-and-getting-stuff-done/) (Feb 2026)
- [Esteban Torres -- A GSD System for Claude Code](https://estebantorr.es/blog/2026/2026-02-03-a-gsd-system-for-claude-code/) (Feb 2026)
- [Neon Nook -- The Rise of "Get-Shit-Done" AI Frameworks](https://neonnook.substack.com/p/the-rise-of-get-shit-done-ai-product) (2026)
- [Joe Njenga -- I Tested GSD Claude Code](https://medium.com/@joe.njenga/i-tested-gsd-claude-code-meta-prompting-that-ships-faster-no-agile-bs-ca62aff18c04) (Jan 2026)
- [Composio -- Top Claude Code Plugins 2026](https://composio.dev/blog/top-claude-code-plugins)
- [Pasquale Pillitteri -- Goodbye Vibe Coding: SDD Framework Guide](https://pasqualepillitteri.it/en/news/158/framework-ai-spec-driven-development-guide-bmad-gsd-ralph-loop) (2026)
- [ccforeveryone.com -- GSD Interactive Lesson](https://ccforeveryone.com/gsd)
- [GSD DeepWiki -- Troubleshooting](https://deepwiki.com/glittercowboy/get-shit-done/16-troubleshooting)
- [Marmelab -- Spec-Driven Development: The Waterfall Strikes Back](https://marmelab.com/blog/2025/11/12/spec-driven-development-waterfall-strikes-back.html)
- [Thoughtworks -- Spec-driven development: Unpacking 2025's key practices](https://www.thoughtworks.com/en-us/insights/blog/agile-engineering-practices/spec-driven-development-unpacking-2025-new-engineering-practices)
- [Rick Hightower -- Claude Code Todos to Tasks](https://medium.com/@richardhightower/claude-code-todos-to-tasks-5a1b0e351a1c) (Jan 2026)

---
*Last updated: 2026-02-17*
