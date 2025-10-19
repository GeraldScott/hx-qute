# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus+HTMX prototype application that demonstrates building modern web applications using:
- Quarkus Java framework with REST endpoints
- Qute template engine for server-side rendering
- HTMX for SPA-like client interactions without complex JavaScript

## Key Commands

### Development
```bash
# Start development mode with live reload
./mvnw compile quarkus:dev

# Alternative with Quarkus CLI
quarkus dev
```
- Application runs at http://localhost:8080
- Dev UI available at http://localhost:8080/q/dev/

### Building
```bash
# Standard build (produces layered JAR)
./mvnw package

# Run the layered JAR
java -jar target/quarkus-app/quarkus-run.jar

# Build uber-jar
./mvnw package -Dquarkus.package.jar.type=uber-jar

# Run uber-jar
java -jar target/*-runner.jar
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify
```

### Native Builds
```bash
# Build native executable (requires GraalVM)
./mvnw package -Dnative

# Build native with Docker container
./mvnw package -Dnative -Dquarkus.native.container-build=true

# Run native executable
./target/hx-qute-1.0.0-runner
```

## Architecture

### Package Structure
- `io.archton.scaffold.router` - REST endpoint resources
- Templates located in `src/main/resources/templates/{ResourceClass}/`

### Template Integration
- Uses Quarkus Qute with `@CheckedTemplate` for compile-time validation
- Template structure: `Templates.{methodName}()` static methods
- Templates automatically mapped to `src/main/resources/templates/{ResourceClass}/{methodName}.html`

### Key Technologies
- Java 17 target
- Quarkus 3.26.2
- Maven-based build system
- Qute template engine with type-safe templates

## Configuration
- Main config: `src/main/resources/application.properties`
- Currently minimal configuration (console log darkening enabled)
