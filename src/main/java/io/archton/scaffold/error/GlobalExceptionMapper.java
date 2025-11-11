package io.archton.scaffold.error;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
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
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Inject
    @Location("error.html")
    Template errorTemplate;

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        // Determine status code and message
        int status;
        String statusText;
        String message;

        if (exception instanceof WebApplicationException wae) {
            Response response = wae.getResponse();
            status = response.getStatus();
            statusText = Response.Status.fromStatusCode(status).getReasonPhrase();
            message = wae.getMessage() != null ? wae.getMessage() : statusText;
        } else {
            status = 500;
            statusText = "Internal Server Error";
            message = "An unexpected error occurred. Please try again later.";
        }

        // Generate unique reference ID for troubleshooting
        String referenceId = UUID.randomUUID().toString();

        // Log the error
        LOG.errorf(
            exception,
            "Error %d [%s]: %s | Path: %s",
            status,
            referenceId,
            exception.getMessage(),
            uriInfo != null ? uriInfo.getPath() : "unknown"
        );

        // Check if request accepts HTML
        boolean acceptsHtml = headers
            .getAcceptableMediaTypes()
            .stream()
            .anyMatch(mediaType ->
                mediaType.isCompatible(MediaType.TEXT_HTML_TYPE) ||
                mediaType.isCompatible(MediaType.WILDCARD_TYPE)
            );

        // Return HTML error page for browser requests
        if (acceptsHtml) {
            boolean devMode = "dev".equals(profile);
            String stackTrace = devMode ? getStackTrace(exception) : null;
            String path = uriInfo != null ? uriInfo.getPath() : null;

            String html = errorTemplate
                .data("status", status)
                .data("statusText", statusText)
                .data("message", message)
                .data("path", path)
                .data("referenceId", referenceId)
                .data("devMode", devMode)
                .data("stackTrace", stackTrace)
                .render();

            return Response.status(status).entity(html).type(MediaType.TEXT_HTML).build();
        }

        // For API requests, return JSON error
        return Response
            .status(status)
            .entity(
                String.format(
                    "{\"error\":\"%s\",\"message\":\"%s\",\"referenceId\":\"%s\"}",
                    statusText,
                    message.replace("\"", "\\\""),
                    referenceId
                )
            )
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
