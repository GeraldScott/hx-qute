package io.archton.scaffold.router;

import io.archton.scaffold.repository.TitleRepository;
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
 * delete endpoint's referential-integrity guard (a title referenced by a person
 * must not be deletable), plus the happy create/delete path as a regression guard.
 */
@QuarkusTest
class TitleResourceTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "MyAdminPassword";
    private static final String TEST_CODE = "ZZ";
    private static final String TEST_DESCRIPTION = "TDD Test Title";

    @Inject
    TitleRepository titleRepository;

    /** Resource tests commit, so remove any leftover test row before and after. */
    @BeforeEach
    @AfterEach
    void removeTestData() {
        QuarkusTransaction.requiringNew().run(() ->
                titleRepository.delete("code = ?1 or description = ?2", TEST_CODE, TEST_DESCRIPTION));
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
    void delete_rejectsTitleReferencedByPerson() {
        // Pick a seed title that a person references — deleting it would violate the
        // fk_person_title constraint, so the endpoint must refuse gracefully.
        Long inUseId = QuarkusTransaction.requiringNew().call(() ->
                titleRepository.listAll().stream()
                        .filter(t -> titleRepository.countPersonReferences(t.id) > 0)
                        .map(t -> t.id)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("seed data should reference at least one title")));

        String html = asAdmin()
        .when()
                .delete("/titles/" + inUseId)
        .then()
                .statusCode(200)
        .extract().asString();

        Document fragment = Jsoup.parseBodyFragment(html);
        assertTrue(fragment.text().contains("Cannot delete"),
                () -> "expected a referential-integrity rejection in the delete modal, got: " + html);

        boolean stillExists = QuarkusTransaction.requiringNew().call(() ->
                titleRepository.findById(inUseId) != null);
        assertTrue(stillExists, "a referenced title must not be deleted");
    }

    @Test
    void createAndDelete_fullLifecycle() {
        RequestSpecification admin = asAdmin();

        // Create an unreferenced title.
        String created = admin
                .formParam("code", TEST_CODE)
                .formParam("description", TEST_DESCRIPTION)
        .when()
                .post("/titles")
        .then()
                .statusCode(200)
        .extract().asString();
        assertTrue(created.contains("hx-swap-oob=\"innerHTML\""),
                () -> "expected OOB table refresh, got: " + created);

        Document doc = Jsoup.parseBodyFragment(created);
        String rowId = doc.select("tr[id^=title-row-]").stream()
                .filter(tr -> tr.text().contains(TEST_DESCRIPTION))
                .findFirst().orElseThrow()
                .id().replace("title-row-", "");

        // An unreferenced title deletes cleanly (guards against the check over-blocking).
        String deleted = asAdmin()
        .when()
                .delete("/titles/" + rowId)
        .then()
                .statusCode(200)
        .extract().asString();
        assertTrue(deleted.contains("title-row-" + rowId), "delete success should target the removed row OOB");
    }
}
