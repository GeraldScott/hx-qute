Implement the next use case from the current feature:

1. Read specs/PROJECT-PLAN.md to identify current feature folder
2. Read the feature's tasks.md to find the next UC marked 🔲 Not Started
3. Read spec.md for technical requirements
4. Read use-cases.md for the UC description
5. Implement the use case following spec.md patterns
6. Update tasks.md: set UC status to 🔄 In Progress, check off completed implementation tasks
7. Run `curl http://127.0.0.1:9080/q/health` to trigger the dev server reload
8. Use the **e2e-test-runner** subagent with this context:
   ```
   Feature folder: specs/{feature-folder}
   Use case: {UC-ID}
   Application URL: http://localhost:9080

   Run the test cases from test-cases.md that match this use case.
   ```
9. Update tasks.md with test results from e2e-test-runner:
   - Copy the **Test Results** table to the UC's Test Results section
   - If all tests pass: set status to ✅ Complete
   - If any tests fail: set status to ❌ Blocked, note the failures
10. Update specs/PROJECT-PLAN.md:
    - Update "Current Use Case" to the next UC
    - Update "Completed" count in Progress Summary
11. STOP and ask for user feedback before proceeding to next UC
