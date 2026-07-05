package io.archton.scaffold.router;

import io.archton.scaffold.repository.RelationshipRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Resource test modelled on {@link GenderResourceTest}: drives the real form-auth
 * flow with REST Assured and asserts on the rendered HTML with Jsoup. Focus is the
 * delete endpoint's referential-integrity guard — a relationship referenced by a
 * person_relationship link must not be deletable — plus the happy create/delete
 * path as a regression guard.
 */
@QuarkusTest
class RelationshipResourceTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "MyAdminPassword";
    private static final String TEST_CODE = "ZZTEST";
    private static final String TEST_DESCRIPTION = "TDD Test Relationship";

    @Inject
    RelationshipRepository relationshipRepository;

    /** Resource tests commit, so remove any leftover test row before and after. */
    @BeforeEach
    @AfterEach
    void removeTestData() {
        QuarkusTransaction.requiringNew().run(() ->
                relationshipRepository.delete("code = ?1 or description = ?2", TEST_CODE, TEST_DESCRIPTION));
    }

    /** Log in through /j_security_check and return a spec carrying the auth cookie. */
    private RequestSpecification asAdmin() {
        CookieFilter cookies = new CookieFilter();
        given()
                .filter(cookies)
                .redirects().follow(false)
                .formParam("j_username", ADMIN_EMAIL)
                .formParam("j_password", ADMIN_PASSWORD)
        .when()
                .post("/j_security_check")
        .then()
                .statusCode(302);
        return given().filter(cookies);
    }

    @Test
    void delete_rejectsRelationshipReferencedByPersonRelationship() {
        // Pick a seed relationship used by a person_relationship link — deleting it
        // would violate the fk_person_rel_type constraint, so the endpoint must refuse.
        Long inUseId = QuarkusTransaction.requiringNew().call(() ->
                relationshipRepository.listAll().stream()
                        .filter(r -> relationshipRepository.countPersonRelationshipReferences(r.id) > 0)
                        .map(r -> r.id)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("seed data should reference at least one relationship")));

        String html = asAdmin()
        .when()
                .delete("/relationships/" + inUseId)
        .then()
                .statusCode(200)
        .extract().asString();

        Document fragment = Jsoup.parseBodyFragment(html);
        assertTrue(fragment.text().contains("Cannot delete"),
                () -> "expected a referential-integrity rejection in the delete modal, got: " + html);

        boolean stillExists = QuarkusTransaction.requiringNew().call(() ->
                relationshipRepository.findById(inUseId) != null);
        assertTrue(stillExists, "a referenced relationship must not be deleted");
    }

    @Test
    void createAndDelete_fullLifecycle() {
        RequestSpecification admin = asAdmin();

        // Create an unreferenced relationship.
        String created = admin
                .formParam("code", TEST_CODE)
                .formParam("description", TEST_DESCRIPTION)
        .when()
                .post("/relationships")
        .then()
                .statusCode(200)
        .extract().asString();
        assertTrue(created.contains("hx-swap-oob=\"innerHTML\""),
                () -> "expected OOB table refresh, got: " + created);

        Document doc = Jsoup.parseBodyFragment(created);
        String rowId = doc.select("tr[id^=relationship-row-]").stream()
                .filter(tr -> tr.text().contains(TEST_DESCRIPTION))
                .findFirst().orElseThrow()
                .id().replace("relationship-row-", "");

        // An unreferenced relationship deletes cleanly (guards against over-blocking).
        String deleted = asAdmin()
        .when()
                .delete("/relationships/" + rowId)
        .then()
                .statusCode(200)
        .extract().asString();
        assertTrue(deleted.contains("relationship-row-" + rowId), "delete success should target the removed row OOB");
    }
}
