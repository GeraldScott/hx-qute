package io.archton.scaffold.router;

import io.archton.scaffold.entity.UserLogin;
import io.archton.scaffold.service.PasswordValidator;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/")
public class AuthResource {

    @Inject
    PasswordValidator passwordValidator;

    // Simple email regex pattern for validation
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance signup(
            String title,
            String currentPage,
            String userName,
            String error
        );

        public static native TemplateInstance login(
            String title,
            String currentPage,
            String userName,
            String error
        );
    }

    // SIGNUP PAGE - GET
    @GET
    @Path("/signup")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance signupPage(@QueryParam("error") String error) {
        String errorMessage = mapSignupError(error);
        return Templates.signup("Sign Up", "signup", null, errorMessage);
    }


    // LOGIN PAGE - GET
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginPage(@QueryParam("error") String error) {
        String errorMessage = null;
        if ("true".equals(error)) {
            errorMessage = "Invalid email or password.";
        }
        return Templates.login("Login", "login", null, errorMessage);
    }

    // SIGNUP - POST
    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response signup(
            @FormParam("email") String email,
            @FormParam("password") String password) {

        // Validate email required
        if (email == null || email.trim().isEmpty()) {
            return Response.seeOther(URI.create("/signup?error=email_required")).build();
        }

        String trimmedEmail = email.trim();

        // Validate email format
        if (!trimmedEmail.matches(EMAIL_PATTERN)) {
            return Response.seeOther(URI.create("/signup?error=email_invalid")).build();
        }

        // Validate password required
        if (password == null || password.isEmpty()) {
            return Response.seeOther(URI.create("/signup?error=password_required")).build();
        }

        // Validate password using PasswordValidator
        List<String> passwordErrors = passwordValidator.validate(password);
        if (!passwordErrors.isEmpty()) {
            // Check which error occurred
            if (password.length() < 15) {
                return Response.seeOther(URI.create("/signup?error=password_short")).build();
            } else if (password.length() > 128) {
                return Response.seeOther(URI.create("/signup?error=password_long")).build();
            }
            // Default password error
            return Response.seeOther(URI.create("/signup?error=password_required")).build();
        }

        // Check for duplicate email (case-insensitive)
        if (UserLogin.emailExists(trimmedEmail)) {
            return Response.seeOther(URI.create("/signup?error=email_exists")).build();
        }

        // Create user with default 'user' role
        UserLogin user = UserLogin.create(trimmedEmail, password, "user");
        user.persist();

        // Redirect to login page on success
        return Response.seeOther(URI.create("/login")).build();
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
