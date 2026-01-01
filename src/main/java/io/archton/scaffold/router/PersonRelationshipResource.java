package io.archton.scaffold.router;

import io.archton.scaffold.entity.Person;
import io.archton.scaffold.entity.PersonRelationship;
import io.archton.scaffold.entity.Relationship;
import io.archton.scaffold.repository.PersonRelationshipRepository;
import io.archton.scaffold.repository.PersonRepository;
import io.archton.scaffold.repository.RelationshipRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/persons/{personId}/relationships")
@RolesAllowed({"user", "admin"})
public class PersonRelationshipResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    @Inject
    RelationshipRepository relationshipRepository;

    @CheckedTemplate
    public static class Templates {
        // Full page
        public static native TemplateInstance personRelationship(
            String title,
            String currentPage,
            String userName,
            Person sourcePerson,
            List<PersonRelationship> relationships,
            List<Person> personChoices,
            List<Relationship> relationshipChoices,
            String filterText,
            String sortField,
            String sortDir
        );

        // Fragments
        public static native TemplateInstance personRelationship$table(
            Person sourcePerson,
            List<PersonRelationship> relationships,
            String filterText
        );

        public static native TemplateInstance personRelationship$modal_create(
            Person sourcePerson,
            PersonRelationship personRelationship,
            List<Person> personChoices,
            List<Relationship> relationshipChoices,
            String error
        );

        public static native TemplateInstance personRelationship$modal_edit(
            Person sourcePerson,
            PersonRelationship personRelationship,
            List<Person> personChoices,
            List<Relationship> relationshipChoices,
            String error
        );

        public static native TemplateInstance personRelationship$modal_delete(
            Person sourcePerson,
            PersonRelationship personRelationship,
            String error
        );

        public static native TemplateInstance personRelationship$modal_success(
            String message,
            Person sourcePerson,
            List<PersonRelationship> relationships,
            String filterText
        );

        public static native TemplateInstance personRelationship$modal_success_row(
            String message,
            PersonRelationship personRelationship
        );

        public static native TemplateInstance personRelationship$modal_delete_success(
            Long deletedId
        );
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response createForm(@PathParam("personId") Long personId) {
        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            return Response.seeOther(URI.create("/persons")).build();
        }

        // Get person choices (all except source person) with titles for display name
        List<Person> personChoices = personRepository.listAllOrderedWithTitle().stream()
            .filter(p -> !p.id.equals(personId))
            .toList();

        List<Relationship> relationshipChoices = relationshipRepository.listAllOrdered();

        return Response.ok(Templates.personRelationship$modal_create(
            sourcePerson,
            new PersonRelationship(),
            personChoices,
            relationshipChoices,
            null
        )).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response create(
            @PathParam("personId") Long personId,
            @FormParam("relatedPersonId") Long relatedPersonId,
            @FormParam("relationshipId") Long relationshipId) {

        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            return Response.seeOther(URI.create("/persons")).build();
        }

        // Get choices for re-rendering form on error (with titles for display name)
        List<Person> personChoices = personRepository.listAllOrderedWithTitle().stream()
            .filter(p -> !p.id.equals(personId))
            .toList();
        List<Relationship> relationshipChoices = relationshipRepository.listAllOrdered();

        // Build a partial entity for form re-population
        PersonRelationship formData = new PersonRelationship();
        if (relatedPersonId != null) {
            formData.relatedPerson = personRepository.findById(relatedPersonId);
        }
        if (relationshipId != null) {
            formData.relationship = relationshipRepository.findById(relationshipId);
        }

        // Validate relatedPersonId
        if (relatedPersonId == null) {
            return Response.ok(Templates.personRelationship$modal_create(
                sourcePerson, formData, personChoices, relationshipChoices,
                "Please select a person."
            )).build();
        }

        // Validate relationshipId
        if (relationshipId == null) {
            return Response.ok(Templates.personRelationship$modal_create(
                sourcePerson, formData, personChoices, relationshipChoices,
                "Please select a relationship type."
            )).build();
        }

        // Check for duplicate relationship
        if (personRelationshipRepository.exists(personId, relatedPersonId, relationshipId)) {
            return Response.ok(Templates.personRelationship$modal_create(
                sourcePerson, formData, personChoices, relationshipChoices,
                "This relationship already exists."
            )).build();
        }

        // Fetch related entities
        Person relatedPerson = personRepository.findById(relatedPersonId);
        Relationship relationship = relationshipRepository.findById(relationshipId);

        if (relatedPerson == null || relationship == null) {
            return Response.ok(Templates.personRelationship$modal_create(
                sourcePerson, formData, personChoices, relationshipChoices,
                "Invalid selection."
            )).build();
        }

        // Create new relationship
        PersonRelationship newRelationship = new PersonRelationship();
        newRelationship.sourcePerson = sourcePerson;
        newRelationship.relatedPerson = relatedPerson;
        newRelationship.relationship = relationship;

        String userName = securityIdentity.getPrincipal().getName();
        newRelationship.createdBy = userName;
        newRelationship.updatedBy = userName;

        personRelationshipRepository.persist(newRelationship);

        // Refresh relationships list
        List<PersonRelationship> relationships = personRelationshipRepository
            .findBySourcePersonWithFilter(personId, null, null, null);

        return Response.ok(Templates.personRelationship$modal_success(
            "Relationship created successfully.",
            sourcePerson,
            relationships,
            null
        )).build();
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response editForm(
            @PathParam("personId") Long personId,
            @PathParam("id") Long id) {

        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            return Response.seeOther(URI.create("/persons")).build();
        }

        PersonRelationship relationship = personRelationshipRepository.findById(id);
        if (relationship == null || !relationship.sourcePerson.id.equals(personId)) {
            return Response.ok(Templates.personRelationship$modal_edit(
                sourcePerson,
                new PersonRelationship(),
                List.of(),
                List.of(),
                "Relationship not found."
            )).build();
        }

        // Get person choices (all except source person) with titles for display name
        List<Person> personChoices = personRepository.listAllOrderedWithTitle().stream()
            .filter(p -> !p.id.equals(personId))
            .toList();

        List<Relationship> relationshipChoices = relationshipRepository.listAllOrdered();

        return Response.ok(Templates.personRelationship$modal_edit(
            sourcePerson,
            relationship,
            personChoices,
            relationshipChoices,
            null
        )).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response update(
            @PathParam("personId") Long personId,
            @PathParam("id") Long id,
            @FormParam("relatedPersonId") Long relatedPersonId,
            @FormParam("relationshipId") Long relationshipId) {

        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            return Response.seeOther(URI.create("/persons")).build();
        }

        PersonRelationship existingRelationship = personRelationshipRepository.findById(id);
        if (existingRelationship == null || !existingRelationship.sourcePerson.id.equals(personId)) {
            return Response.ok(Templates.personRelationship$modal_edit(
                sourcePerson,
                new PersonRelationship(),
                List.of(),
                List.of(),
                "Relationship not found."
            )).build();
        }

        // Get choices for re-rendering form on error (with titles for display name)
        List<Person> personChoices = personRepository.listAllOrderedWithTitle().stream()
            .filter(p -> !p.id.equals(personId))
            .toList();
        List<Relationship> relationshipChoices = relationshipRepository.listAllOrdered();

        // Build form data for repopulation on error
        PersonRelationship formData = new PersonRelationship();
        formData.id = existingRelationship.id;
        formData.sourcePerson = existingRelationship.sourcePerson;
        formData.createdAt = existingRelationship.createdAt;
        formData.createdBy = existingRelationship.createdBy;
        formData.updatedAt = existingRelationship.updatedAt;
        formData.updatedBy = existingRelationship.updatedBy;
        if (relatedPersonId != null) {
            formData.relatedPerson = personRepository.findById(relatedPersonId);
        }
        if (relationshipId != null) {
            formData.relationship = relationshipRepository.findById(relationshipId);
        }

        // Validate relatedPersonId
        if (relatedPersonId == null) {
            return Response.ok(Templates.personRelationship$modal_edit(
                sourcePerson, formData, personChoices, relationshipChoices,
                "Please select a person."
            )).build();
        }

        // Validate relationshipId
        if (relationshipId == null) {
            return Response.ok(Templates.personRelationship$modal_edit(
                sourcePerson, formData, personChoices, relationshipChoices,
                "Please select a relationship type."
            )).build();
        }

        // Check for duplicate relationship (excluding current record)
        if (personRelationshipRepository.existsExcluding(personId, relatedPersonId, relationshipId, id)) {
            return Response.ok(Templates.personRelationship$modal_edit(
                sourcePerson, formData, personChoices, relationshipChoices,
                "This relationship already exists."
            )).build();
        }

        // Fetch related entities
        Person relatedPerson = personRepository.findById(relatedPersonId);
        Relationship relationship = relationshipRepository.findById(relationshipId);

        if (relatedPerson == null || relationship == null) {
            return Response.ok(Templates.personRelationship$modal_edit(
                sourcePerson, formData, personChoices, relationshipChoices,
                "Invalid selection."
            )).build();
        }

        // Update the relationship
        existingRelationship.relatedPerson = relatedPerson;
        existingRelationship.relationship = relationship;
        existingRelationship.updatedBy = securityIdentity.getPrincipal().getName();
        // Note: updatedAt is set automatically by @PreUpdate callback

        return Response.ok(Templates.personRelationship$modal_success_row(
            "Relationship updated successfully.",
            existingRelationship
        )).build();
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteConfirm(
            @PathParam("personId") Long personId,
            @PathParam("id") Long id) {

        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            return Response.seeOther(URI.create("/persons")).build();
        }

        PersonRelationship relationship = personRelationshipRepository.findById(id);
        if (relationship == null || !relationship.sourcePerson.id.equals(personId)) {
            return Response.ok(Templates.personRelationship$modal_delete(
                sourcePerson,
                new PersonRelationship(),
                "Relationship not found."
            )).build();
        }

        return Response.ok(Templates.personRelationship$modal_delete(
            sourcePerson,
            relationship,
            null
        )).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response delete(
            @PathParam("personId") Long personId,
            @PathParam("id") Long id) {

        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            return Response.seeOther(URI.create("/persons")).build();
        }

        PersonRelationship relationship = personRelationshipRepository.findById(id);
        if (relationship == null || !relationship.sourcePerson.id.equals(personId)) {
            return Response.ok(Templates.personRelationship$modal_delete(
                sourcePerson,
                new PersonRelationship(),
                "Relationship not found."
            )).build();
        }

        Long deletedId = relationship.id;
        personRelationshipRepository.delete(relationship);

        return Response.ok(Templates.personRelationship$modal_delete_success(deletedId)).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response list(
            @PathParam("personId") Long personId,
            @HeaderParam("HX-Request") String hxRequest,
            @QueryParam("filter") String filter,
            @QueryParam("sortField") String sortField,
            @QueryParam("sortDir") String sortDir) {

        Person sourcePerson = personRepository.findById(personId);
        if (sourcePerson == null) {
            // Redirect to persons list if person not found
            return Response.seeOther(URI.create("/persons")).build();
        }

        List<PersonRelationship> relationships = personRelationshipRepository
            .findBySourcePersonWithFilter(personId, filter, sortField, sortDir);

        // If HTMX request, return only the table fragment
        if ("true".equals(hxRequest)) {
            return Response.ok(Templates.personRelationship$table(sourcePerson, relationships, filter)).build();
        }

        // Full page request
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();

        // Get person choices (all except source person) with titles for display name
        List<Person> personChoices = personRepository.listAllOrderedWithTitle().stream()
            .filter(p -> !p.id.equals(personId))
            .toList();

        List<Relationship> relationshipChoices = relationshipRepository.listAllOrdered();

        return Response.ok(Templates.personRelationship(
            "Relationships for " + sourcePerson.getDisplayName(),
            "persons",
            userName,
            sourcePerson,
            relationships,
            personChoices,
            relationshipChoices,
            filter,
            sortField,
            sortDir
        )).build();
    }
}
