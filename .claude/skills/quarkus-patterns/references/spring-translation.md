# Spring → Quarkus Translation Tables

Load this when porting Spring code into the project or auditing code for Spring leaks. Every left-column construct is a defect in this codebase; the right column is the replacement actually used here.

## Dependency Injection

| Spring | Quarkus (this project) |
|--------|------------------------|
| `@Autowired` field/setter | `@Inject` on a package-private field |
| `@Component` / `@Service` / `@Repository` | A scope annotation — `@ApplicationScoped`. There are no role stereotypes; scope is the declaration |
| `@Qualifier("x")` | `@Named("x")` or a custom CDI qualifier |
| `@Configuration` + `@Bean` methods | `@ApplicationScoped` bean with `@Produces` methods |
| `@Primary` | `@Alternative` + `@Priority`, or restructure with `@DefaultBean` |
| `@ConditionalOnMissingBean` | `@DefaultBean` (io.quarkus.arc) |
| `@ConditionalOnProperty` | `@IfBuildProfile` / `@UnlessBuildProfile`, or a `@Produces` method branching on config |
| `@Profile("dev")` | `@IfBuildProfile("dev")` — resolved at **build time** |
| `@ComponentScan`, `@Import`, `@DependsOn`, `@Order` | Nothing — discovery is automatic at build time; ordering via `@Priority` |
| `ApplicationContext.getBean(...)` | `@Inject`; for dynamic lookup `CDI.current().select(...)` + `@Unremovable` on the target |
| `@Autowired List<MyInterface>` | `@Inject Instance<MyInterface>` then `.stream()` |
| `@PostConstruct` eager init on singletons | Beans are lazy — `void onStart(@Observes StartupEvent ev)` for startup work |

## Web Layer

| Spring MVC | JAX-RS (Quarkus REST, this project) |
|------------|-------------------------------------|
| `@RestController` + `@RequestMapping("/x")` | `@Path("/x")` on a plain class in `router` |
| `@GetMapping("/y")` | `@GET @Path("/y")` |
| `@PostMapping` / `@PutMapping` / `@DeleteMapping` | `@POST` / `@PUT` / `@DELETE` |
| `@PathVariable` | `@PathParam` |
| `@RequestParam` | `@QueryParam` (+ `@DefaultValue`) |
| `@RequestBody` | The unannotated body parameter; HTML forms use `@FormParam` |
| `ResponseEntity<T>` | Return the entity, a `TemplateInstance` (this project's norm), or a `Response` builder |
| `@ControllerAdvice` + `@ExceptionHandler` | `ExceptionMapper<E>` with `@Provider` — see `GlobalExceptionMapper` |
| `produces = MediaType.TEXT_HTML_VALUE` | `@Produces(MediaType.TEXT_HTML)` |
| `SecurityContextHolder.getContext().getAuthentication()` | `@Inject SecurityIdentity` (+ the project's `isAnonymous() ? "system" : …getName()` idiom) |
| `@PreAuthorize("hasRole('ADMIN')")` | `@RolesAllowed("admin")` + the path entry in `quarkus.http.auth.permission.*` |

## Persistence

| Spring Data | Panache (this project) |
|-------------|------------------------|
| `interface X extends JpaRepository<E, Long>` | `@ApplicationScoped class XRepository implements PanacheRepository<E>` |
| Derived queries (`findByEmailAndActiveTrue()`) | Explicit HQL shorthand: `find("email = ?1 and active = true", email)` — derived names do nothing |
| `@Query("select …")` | Method on the repository using `find`/`list`/`count`/`delete` shorthand |
| `deleteByCreatedAtBefore(cutoff)` | `delete("createdAt < ?1", cutoff)` |
| `org.springframework...@Transactional(readOnly = true)` | `jakarta.transaction.Transactional` — no `readOnly`; omit the annotation on pure reads |
| `@Transactional(rollbackFor = X.class)` | `@Transactional(rollbackOn = X.class)` |
| `Page`/`Pageable` | `PanacheQuery.page(Page.of(index, size))` — see `PersonResource.list` |

## Configuration

| Spring | Quarkus (this project) |
|--------|------------------------|
| `@Value("${x:default}")` | `@ConfigProperty(name = "x", defaultValue = "default")` — no SpEL |
| `@ConfigurationProperties(prefix = "x")` class | `@ConfigMapping(prefix = "x")` **interface**, kebab-case keys, `@WithDefault` |
| `application-dev.yml` / `application-prod.yml` | `%dev.` / `%prod.` prefixes in the single `application.properties` |
| `@Profile`-gated `@Bean` | `@IfBuildProfile` bean or `%profile.` config |
| SpEL in config | Property expressions only: `${other.key:fallback}` |

## Events

| Spring | Quarkus |
|--------|---------|
| `ApplicationEventPublisher.publishEvent(e)` | `@Inject Event<E> event; event.fire(e)` |
| `@EventListener` | `void on(@Observes E e)` |
| `@TransactionalEventListener(phase = AFTER_COMMIT)` | `@Observes(during = TransactionPhase.AFTER_SUCCESS)` |
| `@Async @EventListener` | `@ObservesAsync` + `event.fireAsync(e)` |

## Scheduling

| Spring | Quarkus |
|--------|---------|
| `@EnableScheduling` | Nothing — add the `quarkus-scheduler` extension |
| `@Scheduled(cron = "0 30 3 * * *")` | `@Scheduled(cron = "0 30 3 * * ?")` — Quartz syntax: `?` required in day-of-week when day-of-month is `*` |
| `@Scheduled(fixedRate = 60000)` | `@Scheduled(every = "60s")` |
| (no equivalent) | Always add `identity = "…"` and `concurrentExecution = SKIP` per project convention |

## Testing

| Spring | Quarkus (this project) |
|--------|------------------------|
| `@SpringBootTest(webEnvironment = RANDOM_PORT)` | `@QuarkusTest` — real app boots once; REST Assured pre-targeted at it |
| `@MockBean` | `@InjectMock` — import `io.quarkus.test.junit.mockito.InjectMock`, **not** the Mockito one |
| `MockMvc` | REST Assured `given()…when()…then()` |
| `@ActiveProfiles("test")` | Automatic `%test.` config; `@TestProfile(X.class)` for a custom profile (restarts the app per profile) |
| `@Transactional` test rollback | `@TestTransaction` |
| `@DataJpaTest` + H2 | `@QuarkusTest` + Dev Services (real PostgreSQL, zero config) |
| Manual Testcontainers `@DynamicPropertySource` | Delete it — Dev Services starts the container when no datasource URL is configured |

## Build

| Spring | Quarkus |
|--------|---------|
| `spring-boot-starter-*` | `quarkus-*` extensions via `./mvnw quarkus:add-extension` |
| `spring-boot-maven-plugin` repackage | `quarkus-maven-plugin` build goal (already configured) |
| Fat JAR by default | Fast-jar layout by default; uber-jar and native are opt-in flags |
