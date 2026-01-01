package io.archton.scaffold.router;

import io.archton.scaffold.entity.Title;
import io.archton.scaffold.repository.TitleRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/titles")
@RolesAllowed("admin")
public class TitleResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    TitleRepository titleRepository;

    @CheckedTemplate
    public static class Templates {
        // Full page
        public static native TemplateInstance title(
            String title,
            String currentPage,
            String userName,
            List<Title> titles
        );

        // Fragments (type-safe, compile-time validated)
        public static native TemplateInstance title$table(List<Title> titles);

        // Modal content fragments
        public static native TemplateInstance title$modal_create(Title title, String error);
        public static native TemplateInstance title$modal_edit(Title title, String error);

        // Success response fragments (close modal + OOB updates)
        public static native TemplateInstance title$modal_success(String message, List<Title> titles);
        public static native TemplateInstance title$modal_success_row(String message, Title title);

        // Delete modal fragments
        public static native TemplateInstance title$modal_delete(Title title, String error);
        public static native TemplateInstance title$modal_delete_success(Long deletedId);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Title> titles = titleRepository.listAllOrdered();

        // If HTMX request, return only the table fragment
        if ("true".equals(hxRequest)) {
            return Templates.title$table(titles);
        }

        // Full page request
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
        return Templates.title("Title Management", "title", userName, titles);
    }


    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        // Return empty title object for form binding
        return Templates.title$modal_create(new Title(), null);
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Title title = titleRepository.findById(id);
        if (title == null) {
            // Return error in modal if title not found
            Title emptyTitle = new Title();
            emptyTitle.id = id;
            return Templates.title$modal_edit(emptyTitle, "Title not found.");
        }
        return Templates.title$modal_edit(title, null);
    }


    @POST
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance create(
            @FormParam("code") String code,
            @FormParam("description") String description) {

        Title title = new Title();
        title.code = code;
        title.description = description;

        // Validation
        if (code == null || code.isBlank()) {
            return Templates.title$modal_create(title, "Code is required.");
        }

        if (code.length() > 5) {
            return Templates.title$modal_create(title, "Code must be at most 5 characters.");
        }

        if (description == null || description.isBlank()) {
            return Templates.title$modal_create(title, "Description is required.");
        }

        // Coerce code to uppercase
        title.code = code.toUpperCase();

        // Check uniqueness
        if (titleRepository.findByCode(title.code).isPresent()) {
            return Templates.title$modal_create(title, "Code already exists.");
        }

        if (titleRepository.findByDescription(description).isPresent()) {
            return Templates.title$modal_create(title, "Description already exists.");
        }

        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        title.createdBy = userName;
        title.updatedBy = userName;

        // Persist
        titleRepository.persist(title);

        // Return success with OOB table refresh
        List<Title> titles = titleRepository.listAllOrdered();
        return Templates.title$modal_success("Title created successfully.", titles);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("code") String code,
            @FormParam("description") String description) {

        Title title = titleRepository.findById(id);
        if (title == null) {
            Title emptyTitle = new Title();
            emptyTitle.id = id;
            return Templates.title$modal_edit(emptyTitle, "Title not found.");
        }

        // Store original values for validation
        String originalCode = title.code;
        String originalDescription = title.description;

        // Update title object with form values for re-display
        title.code = code;
        title.description = description;

        // Validation
        if (code == null || code.isBlank()) {
            return Templates.title$modal_edit(title, "Code is required.");
        }

        if (code.length() > 5) {
            return Templates.title$modal_edit(title, "Code must be at most 5 characters.");
        }

        if (description == null || description.isBlank()) {
            return Templates.title$modal_edit(title, "Description is required.");
        }

        // Coerce code to uppercase
        title.code = code.toUpperCase();

        // Check uniqueness (excluding current record)
        var existingByCode = titleRepository.findByCode(title.code);
        if (existingByCode.isPresent() && !existingByCode.get().id.equals(id)) {
            return Templates.title$modal_edit(title, "Code already exists.");
        }

        var existingByDescription = titleRepository.findByDescription(description);
        if (existingByDescription.isPresent() && !existingByDescription.get().id.equals(id)) {
            return Templates.title$modal_edit(title, "Description already exists.");
        }

        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        title.updatedBy = userName;

        // Persist changes (entity is already managed, changes will be flushed)

        // Return success with OOB single row update
        return Templates.title$modal_success_row("Title updated successfully.", title);
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance deleteConfirm(@PathParam("id") Long id) {
        Title title = titleRepository.findById(id);
        if (title == null) {
            Title emptyTitle = new Title();
            emptyTitle.id = id;
            return Templates.title$modal_delete(emptyTitle, "Title not found.");
        }
        return Templates.title$modal_delete(title, null);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance delete(@PathParam("id") Long id) {
        Title title = titleRepository.findById(id);
        if (title == null) {
            Title emptyTitle = new Title();
            emptyTitle.id = id;
            return Templates.title$modal_delete(emptyTitle, "Title not found.");
        }

        // TODO: Check if title is in use by Person records
        // For now, just delete - Person entity doesn't have title field yet
        // Long personCount = Person.count("title", title);
        // if (personCount > 0) {
        //     return Templates.title$modal_delete(title,
        //         "Cannot delete: Title is in use by " + personCount + " person(s).");
        // }

        titleRepository.delete(title);

        // Return success with OOB row removal
        return Templates.title$modal_delete_success(id);
    }
}
