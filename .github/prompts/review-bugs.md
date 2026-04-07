# Agent: review-bugs
# Purpose: Full code review — bugs, quality, security, and performance

Thoroughly analyze all source code files in this repository and generate a file
named `REPORTE-BUGS.md` at the project root.

## Analysis instructions

### 1. Bugs and logical errors
Search the entire codebase for:
- Logical errors and functional bugs
- Missing input validation (null, negative values, empty strings)
- Incorrect exception handling (empty catch blocks, overly generic exceptions)
- Race conditions or concurrency issues
- Physical deletion where logical deletion should be used
- Loss of precision in numeric operations (double/float for money)
- Potential unhandled NullPointerExceptions

### 2. Code quality
Assess the following dimensions:
- SOLID principles (Single Responsibility, Open/Closed, etc.)
- Duplicated code or violations of DRY (Don't Repeat Yourself)
- Methods that are too long or have too many responsibilities
- Incorrect dependency injection (@Autowired on fields vs constructor)
- Missing documentation (Javadoc/JSDoc) on public methods
- Poorly descriptive variable or method names
- High cyclomatic complexity

### 3. Security (cybersecurity perspective)
Identify security issues:
- SQL Injection: dynamic queries without parameterization
- Exposing JPA entities directly from REST endpoints
- Sensitive data exposed in logs or error responses
- Missing input validation on public endpoints
- Insecure configuration (hardcoded passwords, SSL disabled)
- Cross-Site Scripting (XSS) in the frontend
- Stack traces exposed to the client in error responses
- Secrets or API keys committed in source code

### 4. Performance
Detect performance problems:
- N+1 queries in JPA/Hibernate (misused lazy loading)
- Missing pagination on endpoints that return full lists
- Expensive operations inside loops
- Missing indexes for frequent queries
- Loading full entities when only specific fields are needed
- Unnecessary React re-renders (missing useMemo/useCallback)
- Redundant HTTP calls from the frontend

## Report format

Generate the report using this exact structure:

# Full Code Review Report
**Project:** [project name]
**Date:** [current date]
**Files analyzed:** [count]
**Total findings:** [count]

## Executive summary
[3–4 lines summarizing the most critical findings]

## Statistics
| Category | Critical | High | Medium | Low | Total |
|----------|----------|------|--------|-----|-------|
| Bugs | | | | | |
| Quality | | | | | |
| Security | | | | | |
| Performance | | | | | |
| **Total** | | | | | |

## Detailed findings

### Bugs and logical errors
#### [CRITICAL/HIGH/MEDIUM/LOW] BUG-001: [title]
- **File:** path/to/File.java (line N)
- **Description:** what is wrong and why it matters
- **Impact:** what could happen if left unfixed
- **Suggested fix:** code or steps to resolve it

[repeat for each finding]

### Code quality
[same structure]

### Security
[same structure]

### Performance
[same structure]

## Recommended action plan
A prioritized list of the 5 most critical findings to fix first,
with an effort estimate (hours) for each.

## Note on complementary tools
This analysis was performed by Claude Code. For ongoing static analysis,
complement with:
- SonarQube/SonarCloud: quality and code coverage
- OWASP Dependency-Check: dependency vulnerabilities
- Snyk: dependency and container security
