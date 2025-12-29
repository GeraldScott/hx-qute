Validate the current feature's spec files before implementation:

1. Read specs/PROJECT-PLAN.md to identify the current feature folder
2. Verify all 4 spec files exist in the feature folder:
   - use-cases.md
   - spec.md
   - tasks.md
   - test-cases.md
3. For each UC-FFF-SS-NN in use-cases.md:
   - Verify a matching US-FFF-SS exists in docs/USER-STORIES.md
   - Verify the FFF-SS portion matches the parent user story
4. For each TC-FFF-SS-NNN in test-cases.md:
   - Verify it links back to a valid UC-FFF-SS-NN in use-cases.md
5. Verify specs/PROJECT-PLAN.md references this feature's tasks.md

Report any validation errors found, or confirm all checks passed.
