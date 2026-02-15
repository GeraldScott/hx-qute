# hx-qute: Quarkus+HTMX+Claude

This project uses Quarkus with the Qute template engine to demonstrate how to use HTMX with Quarkus. 

It functions as a test bed for developing and implementing functionality using Claude Skills.

Once the SKILL is tested, it moves to hx-scaffold for general release.

## Technology stack

- Quarkus Java framework
- Java 21
- HTMX for SPA-like client-side interactions 
- Qute template engine
- UIkit 3.25 for styling
- JUnit 5 for testing
- Hibernate ORM with Panache for database access
- Hibernate Validator for input validation
- Quarkus Security JPA for authentication
- PostgreSQL for database
- Flyway for database migrations
- Podman for containerization
- Jaeger for tracing
- Prometheus for monitoring

## Creating this project from scratch

Install the Quarkus CLI using SDKMAN:

```bash
sdk install quarkus
```

Then generate the project:

```bash
quarkus create app \
  --description='Quarkus+HTMX prototype' \
  --extensions='rest-qute,arc,flyway,hibernate-orm-panache,hibernate-validator,jdbc-postgresql,smallrye-health,security-jpa' \
  --maven \
  --app-config='quarkus.log.console.darken=1' \
  --no-code \
  io.archton.scaffold:hx-qute:1.0.0
```

## Packaging and running the application
### Running in DEV mode

Pre-requisite for Testcontainers to run postgresql using Podman. Always run this first.
```bash
systemctl --user enable podman.socket --now
export DOCKER_HOST=unix://$(podman info --format '{{.Host.RemoteSocket.Path}}')
```

Run the application in dev mode to enable live coding:

```bash
quarkus dev
```

- Application is at <http://localhost:9080>
- Browse to the Dev UI: <http://localhost:9080/q/dev/>

> **Note:** Port 9080 is used instead of the default 8080 to avoid conflicts with other local services.

### Default Admin Login

| Field | Value |
|-------|-------|
| Email | `admin@example.com` |
| Password | `AdminPassword123` |

### Packaging  the application
#### Layered package

```bash
./mvnw package
```
 
- Produces `quarkus-run.jar` file in `target/quarkus-app/`
- Dependencies are in `target/quarkus-app/lib/`
- Run with `java -jar target/quarkus-app/quarkus-run.jar`

#### Uber-jar

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```
 
- Run with `java -jar target/*-runner.jar`

#### Native executable

Build with GraalVM installed:

```bash
./mvnw package -Dnative
```

Or run the build in a GraalVM container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

- Run native executable: `./target/hx-qute-1.0.0-runner`

## Related Guides

- Quarkus website: <https://quarkus.io/>.
- REST Qute ([guide](https://quarkus.io/guides/qute-reference#rest_integration)): Qute integration for Quarkus REST. This extension is not compatible with the older classic quarkus-resteasy extension.
- Building native executables: <https://quarkus.io/guides/maven-tooling>.
- Banner: https://patorjk.com/software/taag/#p=display&f=Acrobatic&t=hx-qute
- [Martin Fowler: Anemic Domain Model](https://martinfowler.com/bliki/AnemicDomainModel.html)
- [Steve Ardalis: Should Controllers Reference Repositories or Services](https://ardalis.com/should-controllers-reference-repositories-services/)
- [Quarkus Panache Guide](https://quarkus.io/guides/hibernate-orm-panache)
