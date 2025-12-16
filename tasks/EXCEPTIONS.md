# Code Review: GlobalExceptionMapper.java

## Overview

This document reviews the implementation of `src/main/java/io/archton/scaffold/error/GlobalExceptionMapper.java` against Quarkus error handling best practices.

---

## What the Application Does Well

### 1. Uses the Standard JAX-RS Approach

The `@Provider` annotation and `ExceptionMapper<Throwable>` interface is the correct, supported approach for global exception handling in Quarkus REST. This is fully compatible with both classic RESTEasy and RESTEasy Reactive.

### 2. Content Negotiation

Checking the `Accept` header to return HTML for browsers and JSON for API clients is a recommended practice for applications that serve both web pages and REST APIs.

```java
boolean acceptsHtml = headers
    .getAcceptableMediaTypes()
    .stream()
    .anyMatch(mediaType ->
        mediaType.isCompatible(MediaType.TEXT_HTML_TYPE) ||
        mediaType.isCompatible(MediaType.WILDCARD_TYPE)
    );
```

### 3. Reference ID for Troubleshooting

Generating a UUID reference ID and logging it alongside the error is excellent for production debugging. This allows support teams to correlate user-reported errors with server logs.

```java
String referenceId = UUID.randomUUID().toString();
```

### 4. Security-Conscious in Production

Only showing stack traces in dev mode prevents information leakage. This addresses OWASP WSTG-INFO-08 and WSTG-INFO-10 concerns about revealing framework internals and sensitive paths.

### 5. Proper Handling of WebApplicationException

Extracting the status and message from `WebApplicationException` subclasses preserves intentional error responses set by endpoint code, rather than overriding them with generic 500 errors.

---

## Suggestions for Improvement

### 1. Add `@Priority` Annotation

Add a priority annotation to ensure your mapper takes precedence over built-in Quarkus mappers. There are known issues where custom exception mappers for `Throwable` may not always be called if they conflict with framework defaults.

```java
import jakarta.annotation.Priority;

@Provider
@Priority(1)  // Lower number = higher priority
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
```

### 2. Handle `ConstraintViolationException` Separately

Create a dedicated mapper for validation errors to return structured error messages with field-level details:

```java
package io.archton.scaffold.error;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper 
        implements ExceptionMapper<ConstraintViolationException> {
    
    @Override
    public Response toResponse(ConstraintViolationException e) {
        List<ValidationError> errors = e.getConstraintViolations().stream()
            .map(cv -> new ValidationError(
                cv.getPropertyPath().toString(),
                cv.getMessage()))
            .collect(Collectors.toList());
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ValidationErrorResponse(errors))
            .build();
    }
    
    public record ValidationError(String field, String message) {}
    public record ValidationErrorResponse(List<ValidationError> errors) {}
}
```

### 3. Create a Structured Error Response DTO

Replace the manual JSON string building with a proper DTO for type safety and consistent serialization:

```java
package io.archton.scaffold.error;

import java.time.Instant;

public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    String referenceId,
    Instant timestamp
) {
    public ErrorResponse(int status, String error, String message, 
                         String path, String referenceId) {
        this(status, error, message, path, referenceId, Instant.now());
    }
}
```

Then use it in your mapper:

```java
// For API requests, return JSON error
ErrorResponse errorResponse = new ErrorResponse(
    status,
    statusText,
    message,
    uriInfo != null ? uriInfo.getPath() : null,
    referenceId
);

return Response
    .status(status)
    .entity(errorResponse)
    .type(MediaType.APPLICATION_JSON)
    .build();
```

### 4. Consider Using `@ServerExceptionMapper`

Since the project uses `quarkus-rest-qute` (RESTEasy Reactive), the modern Quarkus-native approach is available:

```java
package io.archton.scaffold.error;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionHandlers {
    
    @ServerExceptionMapper
    public Response handleThrowable(Throwable exception, UriInfo uriInfo) {
        // Your global handling logic
    }
    
    @ServerExceptionMapper
    public Response handleNotFound(NotFoundException exception, UriInfo uriInfo) {
        // Specific 404 handling with custom error page
    }
    
    @ServerExceptionMapper
    public Response handleIllegalArgument(IllegalArgumentException exception) {
        // Return 400 Bad Request
    }
}
```

This approach allows injecting request context parameters directly into handler methods.

### 5. Hide Internal Framework Messages

The current implementation may expose RESTEasy error prefixes (like "RESTEASY003210"), which reveals the web framework being used. Strip these prefixes:

```java
// Strip framework-specific prefixes for security
if (message != null && message.startsWith("RESTEASY")) {
    message = statusText;
}
```

### 6. Add Null Safety for Profile Check

Include test mode in the dev check and handle potential null values:

```java
boolean devMode = "dev".equals(profile) || "test".equals(profile);
```

### 7. Consider Not Catching JVM Errors

Currently catching `Throwable` includes JVM errors like `OutOfMemoryError` which should generally propagate. Consider re-throwing critical errors:

```java
@Override
public Response toResponse(Throwable exception) {
    // Let critical JVM errors propagate
    if (exception instanceof Error && !(exception instanceof AssertionError)) {
        throw (Error) exception;
    }
    
    // ... rest of implementation
}
```

---

## Recommended Refactored Implementation

```java
package io.archton.scaffold.error;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Provider
@Priority(1)
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Inject
    @Location("error.html")
    Template errorTemplate;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        // Let critical JVM errors propagate
        if (exception instanceof Error && !(exception instanceof AssertionError)) {
            throw (Error) exception;
        }

        int status;
        String statusText;
        String message;

        if (exception instanceof WebApplicationException wae) {
            Response response = wae.getResponse();
            status = response.getStatus();
            statusText = Response.Status.fromStatusCode(status).getReasonPhrase();
            message = sanitizeMessage(wae.getMessage(), statusText);
        } else {
            status = 500;
            statusText = "Internal Server Error";
            message = "An unexpected error occurred. Please try again later.";
        }

        String referenceId = UUID.randomUUID().toString();
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";

        LOG.errorf(exception, "Error %d [%s]: %s | Path: %s",
            status, referenceId, exception.getMessage(), path);

        if (acceptsHtml()) {
            return buildHtmlResponse(status, statusText, message, path, referenceId, exception);
        }

        return buildJsonResponse(status, statusText, message, path, referenceId);
    }

    private String sanitizeMessage(String message, String fallback) {
        if (message == null || message.startsWith("RESTEASY")) {
            return fallback;
        }
        return message;
    }

    private boolean acceptsHtml() {
        return headers.getAcceptableMediaTypes().stream()
            .anyMatch(mt -> mt.isCompatible(MediaType.TEXT_HTML_TYPE) 
                         || mt.isCompatible(MediaType.WILDCARD_TYPE));
    }

    private Response buildHtmlResponse(int status, String statusText, String message,
                                        String path, String referenceId, Throwable exception) {
        boolean devMode = "dev".equals(profile) || "test".equals(profile);
        String stackTrace = devMode ? getStackTrace(exception) : null;

        String html = errorTemplate
            .data("status", status)
            .data("statusText", statusText)
            .data("message", message)
            .data("path", path)
            .data("referenceId", referenceId)
            .data("devMode", devMode)
            .data("stackTrace", stackTrace)
            .data("currentPage", null)
            .data("userName", null)
            .render();

        return Response.status(status)
            .entity(html)
            .type(MediaType.TEXT_HTML)
            .build();
    }

    private Response buildJsonResponse(int status, String statusText, 
                                        String message, String path, String referenceId) {
        ErrorResponse errorResponse = new ErrorResponse(
            status, statusText, message, path, referenceId);
        
        return Response.status(status)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        String referenceId
    ) {}
}
```

---

## Overall Assessment

The current implementation follows Quarkus best practices well and is more thoughtful than many examples found in the community. The HTML/JSON content negotiation pattern is exactly what is recommended for applications serving both web pages and APIs.

**Priority improvements:**

1. Add `@Priority(1)` annotation (quick win, prevents mapper conflicts)
2. Create `ErrorResponse` DTO (type safety, consistent JSON)
3. Sanitize framework error messages (security)

**Nice-to-have improvements:**

4. Separate `ConstraintViolationExceptionMapper` for validation
5. Consider migrating to `@ServerExceptionMapper` for reactive-native approach
6. Add specific mappers for common exceptions (404, 400, 403)

---

## References

- [Quarkus REST Guide - Exception Mapping](https://quarkus.io/guides/rest#exception-mapping)
- [Red Hat Developer - REST API Error Modeling with Quarkus](https://developers.redhat.com/articles/2022/03/03/rest-api-error-modeling-quarkus-20)
- [Centralized Error Response Handling in Quarkus](https://marcelkliemannel.com/articles/2021/centralized-error-handling-and-a-custom-error-page-in-quarkus/)
