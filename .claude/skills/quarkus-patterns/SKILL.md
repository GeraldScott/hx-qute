---
name: quarkus-patterns
description: Use when writing Quarkus platform code in this project — CDI beans, scopes, events, scheduled jobs, config binding and profiles, startup lifecycle, or choosing a test mocking strategy — and when porting or reviewing Spring-shaped code.
---

# Quarkus Platform Patterns

Quarkus platform idioms as used in this project. Sibling skills own the layers built on top: `java-patterns` (where code lives), `postgresql-java` (persistence), `htmx-patterns` (web). This skill owns the container itself: DI, events, config, lifecycle, scheduling, and test infrastructure.

## The Mental Model: Build Time, Not Runtime

Spring resolves beans, profiles, and conditions at runtime; Quarkus's ArC container resolves them **at build time** and prunes what it can't see. When something Spring-shaped fails here, ask *when the decision happens*:

- Unreferenced beans are **removed at build time** — a bean only reached via programmatic lookup needs `@Unremovable`
- `@IfBuildProfile`/`@UnlessBuildProfile` include/exclude beans **per build**, not per launch
- Some `quarkus.*` properties are fixed at build time (marked in the docs) — overriding them with env vars at runtime silently does nothing
- Beans in third-party JARs aren't discovered without a Jandex index (`quarkus.index-dependency.*`)

## Critical Rules

- Default scope is `@ApplicationScoped` (client proxy → mockable, lazy). Use `@Singleton` only on measured hot paths.
- `@Inject` on package-private fields is this project's style; no `@Autowired`, no stereotype annotations (`@Service`/`@Component`) — Quarkus is scope-first, not role-first.
- `@Transactional` is `jakarta.transaction.Transactional`: no `readOnly`, and rollback customization is `rollbackOn`/`dontRollbackOn`.
- This project is **fully blocking**: plain return types on worker threads. No Mutiny `Uni`/`Multi` — returning one moves the method to the I/O event loop where JDBC/Panache calls are forbidden.
- No `org.springframework.*` imports, ever. The `quarkus-spring-*` compat extensions are migration bridges and deliberately not used here.
- One config file: `application.properties` with `%dev.`/`%test.`/`%prod.` prefixes — never per-profile files.

## Decision Tables

### Beans and DI

| You need to… | Use |
|--------------|-----|
| Provide a swappable default implementation | `@DefaultBean` on the fallback; declaring any other bean of the type replaces it with no call-site changes |
| Include a bean only in one profile | `@IfBuildProfile("prod")` / `@UnlessBuildProfile("prod")` (build-time — dev builds contain no prod code path) |
| Inject all implementations of an interface | `@Inject Instance<T>`, then `.stream()` |
| Construct a bean from config/third-party code | `@Produces` method on an `@ApplicationScoped` bean |
| Run code once at startup | `void onStart(@Observes StartupEvent ev)` — beans are lazy otherwise |

### Events and Side Effects

Fire `jakarta.enterprise.event.Event<T>.fire(...)` with a **record payload of plain values** (never a managed entity). Choose the observer's transaction phase by the side effect's semantics:

| The side effect must… | Observer signature |
|-----------------------|-------------------|
| Commit or roll back atomically with the triggering change (e.g. audit rows) | `@Transactional void on(@Observes E e)` — joins the caller's transaction |
| Happen only after commit, and never break the caller (e.g. notifications, email) | `void on(@Observes(during = TransactionPhase.AFTER_SUCCESS) E e)` — wrap the body in try/catch |

### Configuration

| Shape | Use | Exemplar |
|-------|-----|----------|
| Single value | `@ConfigProperty(name = "app.x", defaultValue = "…")` | `GlobalExceptionMapper`, `PasswordValidator` |
| Group of related values | `@ConfigMapping(prefix = "app.x")` **interface**, kebab-case keys, `@WithDefault`; Bean Validation annotations are enforced at startup | — |

Custom properties live under the `app.` prefix (`app.security.password.*`). Secrets enter via env-var placeholders in `%prod.` entries, never literals.

### Testing and Mocking

| Situation | Approach |
|-----------|----------|
| Pure logic, dependencies mockable, no CDI/transaction/config behavior under test | Plain JUnit + `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks` — no container boot. **Gotcha:** `@Transactional`, interceptors, and `@ConfigProperty` are all inert |
| Behavior needs the container (config injection, security, interceptors) but a dependency must be faked | `@QuarkusTest` + `@InjectMock` (`io.quarkus.test.junit.mockito.InjectMock` — the frequent wrong-import bug) — works because `@ApplicationScoped` beans are proxied |
| Real HTTP/DB behavior | `@QuarkusTest` + REST Assured / `@TestTransaction` — see the `GenderResourceTest`/`GenderRepositoryTest` exemplars |

Mockito comes from `quarkus-junit5-mockito` (BOM-managed). Dev Services provide test databases automatically when no datasource is configured — don't hand-wire Testcontainers.

## Scheduled Jobs

`quarkus-scheduler` is **not yet in the pom** — add it on first use. Conventions:

```java
@Scheduled(cron = "0 30 3 * * ?", identity = "purge-audit-events",
           concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
@Transactional
void purge() { … }
```

- Quarkus crons are **Quartz syntax**: day-of-week must be `?` when day-of-month is `*` — Spring's `0 30 3 * * *` is invalid here
- Always set `identity` (observability) and `SKIP` (overlap guard); mutating jobs carry `@Transactional`

## Spring-ism Smell Checklist

For reviewing ported or hand-written code (`java-code-review` links here). Any of these is a finding:

1. `org.springframework.*` import, or a `quarkus-spring-*` compat extension in the pom
2. `@Service`/`@Component`/`@Autowired`/`@Value` → scope annotation, `@Inject`, `@ConfigProperty`
3. Repository **interface** with derived query names (`findByEmailAndActiveTrue`) → Panache repository *class* with explicit HQL shorthand
4. `ResponseEntity`, `@RestController`, `@GetMapping` → `TemplateInstance`/`Response`, `@Path`, `@GET`
5. `@MockBean` or `MockMvc` in tests → `@InjectMock` / REST Assured
6. `application-{profile}.properties` files → `%profile.` prefixes in the one file
7. Spring cron strings copied verbatim → check the Quartz `?` rule
8. Manually configured test datasource that Dev Services would provide

Full annotation-by-annotation translation tables: `references/spring-translation.md` — load when porting Spring code or auditing for leaks.

## Related Skills

- `java-patterns` — layering, entity/resource conventions, validation and exceptions
- `postgresql-java` — Panache repository and migration conventions
- `java-code-review` — cites the smell checklist above during review
- `maven-java` — how extensions get added (`./mvnw quarkus:add-extension`)

---

*Quarkus 3.30.3 · Java 21 · Last Updated: 2026-07-04*
