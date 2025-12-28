package io.archton.scaffold.router;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class AuthResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance signup(
            String title,
            String currentPage,
            String userName,
            String error
        );
    }

    // SIGNUP PAGE
    @GET
    @Path("/signup")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance signupPage(@QueryParam("error") String error) {
        String errorMessage = mapSignupError(error);
        return Templates.signup("Sign Up", "signup", null, errorMessage);
    }

    private String mapSignupError(String errorCode) {
        if (errorCode == null) {
            return null;
        }
        return switch (errorCode) {
            case "email_required" -> "Email is required.";
            case "password_required" -> "Password is required.";
            case "password_short" -> "Password must be at least 15 characters.";
            case "password_long" -> "Password must be 128 characters or less.";
            case "email_exists" -> "Email already registered.";
            case "email_invalid" -> "Invalid email format.";
            default -> "An error occurred. Please try again.";
        };
    }
}
