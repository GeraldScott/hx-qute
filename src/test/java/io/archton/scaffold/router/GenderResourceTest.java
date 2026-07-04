package io.archton.scaffold.router;

import io.archton.scaffold.repository.GenderRepository;
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
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemplar resource test: drives the real form-auth flow with REST Assured
 * (same admin seed user as the e2e-test-runner agent — see
 * V1.2.1__Insert_admin_user.sql) and asserts on the rendered HTML with Jsoup.
 */
@QuarkusTest
class GenderResourceTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "AdminPassword123";
    private static final String TEST_CODE = "Y";
    private static final String TEST_DESCRIPTION = "Lifecycle Test";

    @Inject
    GenderRepository genderRepository;

    /**
     * Resource tests exercise real HTTP requests, so writes COMMIT (unlike
     * @TestTransaction repository tests). Every test that creates data must
     * clean up before and after itself to stay repeatable.
     */
    @BeforeEach
    @AfterEach
    void removeTestData() {
        QuarkusTransaction.requiringNew().run(() ->
                genderRepository.delete("code = ?1 or description = ?2", TEST_CODE, TEST_DESCRIPTION));
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
    void list_redirectsAnonymousUsersToLogin() {
        given()
                .redirects().follow(false)
        .when()
                .get("/genders")
        .then()
                .statusCode(302)
                .header("Location", containsString("login"));
    }

    @Test
    void list_rendersFullPageWithSeedData() {
        String html = asAdmin()
        .when()
                .get("/genders")
        .then()
                .statusCode(200)
        .extract().asString();

        Document page = Jsoup.parse(html);
        assertNotNull(page.selectFirst("html"), "Full page request should render the base layout");
        assertNotNull(page.selectFirst("#gender-table-container"), "Entity-prefixed table container expected");
        assertTrue(page.text().contains("Female"), "Seeded gender should be listed");
    }

    @Test
    void list_returnsOnlyTableFragmentForHtmxRequests() {
        String html = asAdmin()
                .header("HX-Request", "true")
        .when()
                .get("/genders")
        .then()
                .statusCode(200)
        .extract().asString();

        Document fragment = Jsoup.parseBodyFragment(html);
        assertTrue(html.trim().length() > 0);
        assertFalse(html.contains("<html"), "HTMX request must return the fragment, not the full page");
        assertNotNull(fragment.selectFirst("table"), "Table fragment expected");
    }

    @Test
    void create_rejectsBlankCodeAndPreservesInput() {
        String html = asAdmin()
                .formParam("code", "")
                .formParam("description", "Test Description")
        .when()
                .post("/genders")
        .then()
                .statusCode(200)
        .extract().asString();

        Document fragment = Jsoup.parseBodyFragment(html);
        assertTrue(fragment.text().contains("Code is required."), "Validation error should be rendered");
        assertEquals("Test Description", fragment.selectFirst("[name=description]").val(),
                "User input must be preserved in the re-rendered form");
    }

    @Test
    void createAndDelete_fullLifecycle() {
        RequestSpecification admin = asAdmin();

        // Create
        String created = admin
                .formParam("code", TEST_CODE)
                .formParam("description", TEST_DESCRIPTION)
        .when()
                .post("/genders")
        .then()
                .statusCode(200)
        .extract().asString();
        // Success = modal-close trigger + OOB table refresh (the message param
        // is not rendered by the fragment; the table content is the signal)
        assertTrue(created.contains("hx-swap-oob=\"innerHTML\""),
                () -> "expected OOB table refresh, got: " + created);

        // Find the new row's id from the OOB table refresh
        Document doc = Jsoup.parseBodyFragment(created);
        String rowId = doc.select("tr[id^=gender-row-]").stream()
                .filter(tr -> tr.text().contains(TEST_DESCRIPTION))
                .findFirst().orElseThrow()
                .id().replace("gender-row-", "");

        // Delete (cleanup + exercises the delete endpoint)
        String deleted = asAdmin()
        .when()
                .delete("/genders/" + rowId)
        .then()
                .statusCode(200)
        .extract().asString();
        assertTrue(deleted.contains("gender-row-" + rowId), "Delete success should target the removed row OOB");
    }
}
