package io.archton.scaffold.router;

import io.archton.scaffold.entity.Person;
import io.archton.scaffold.repository.PersonRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@Path("/persons")
public class PersonResource {

    @Inject
    PersonRepository personRepository;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance person(String title, String userName, List<Person> persons);
        public static native TemplateInstance personModal(Person person);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        List<Person> persons = personRepository.listAll();
        return Templates.person(
            "HX-Qute People",
            "Guest",
            persons
        );
    }

    @GET
    @Path("/{id}/modal")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance personModal(@PathParam("id") Long id) {
        Person person = personRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Person not found"));
        return Templates.personModal(person);
    }
}
