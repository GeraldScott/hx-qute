Implement the next use case from the current feature:
1. Read specs/PROJECT-PLAN.md to identify current feature
2. Read the feature's tasks.md to find the next UC marked 🔲
3. Read spec.md for technical requirements
4. Implement the use case
5. Update tasks.md status to 🔄
6. Run curl http://127.0.0.1:9080/q/health to trigger the reload of the dev server
7. Initiate a sub-agent to execute test-cases.md using chrome-devtools MCP and then update tasks.md with the test results
8. STOP and ask for user feedback
