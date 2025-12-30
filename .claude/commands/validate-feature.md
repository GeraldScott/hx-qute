Validate the current feature's spec files before implementation:

**Input:** Feature number (e.g., 002) provided as: $ARGUMENTS

1. Validate the feature exists in docs/USER-STORIES.md:
   - Search for the feature heading (e.g., ## Feature 002:)
   - If not found, report error and stop
   - Extract all user stories (US-FFF-SS) belonging to this feature

2. Locate the feature folder in specs/:
   - Find folder matching pattern `FFF-*` (e.g., `002-master-data-management`)
   - If no matching folder exists, report error and stop

3. Verify all 4 spec files exist in the feature folder:
   - use-cases.md
   - spec.md
   - tasks.md
   - test-cases.md

4. For each user story (US-FFF-SS) found in step 1:
   - Verify at least one use case (UC-FFF-SS-NN) exists in use-cases.md
   - Report error if any user story has no corresponding use cases

5. For each use case (UC-FFF-SS-NN) in use-cases.md:
   - Verify at least one test case (TC-FFF-SS-NNN) exists in test-cases.md
   - Report error if any use case has no corresponding test cases

6. For each test case (TC-FFF-SS-NNN) in test-cases.md:
   - Verify it links back to a valid UC-FFF-SS-NN in use-cases.md

7. Verify specs/PROJECT-PLAN.md references this feature's tasks.md

Report any validation errors found, or confirm all checks passed.
