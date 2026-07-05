package io.archton.scaffold.router;

import io.archton.scaffold.entity.UserLogin;
import io.archton.scaffold.repository.UserLoginRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * Verifies two-tier RBAC: user-role accounts are forbidden on admin resources,
 * and inactive accounts cannot authenticate at all.
 */
@QuarkusTest
class RbacTest {

    private static final String TEST_USER_EMAIL    = "rbac-test-user@example.com";
    private static final String TEST_USER_PASSWORD = "RbacTestUserPassword";
    private static final String INACTIVE_EMAIL     = "rbac-inactive@example.com";
    private static final String INACTIVE_PASSWORD  = "RbacInactivePassword";

    @Inject
    UserLoginRepository userLoginRepository;

    @BeforeEach
    void createTestUsers() {
        QuarkusTransaction.requiringNew().run(() -> {
            UserLogin active = new UserLogin();
            active.email    = TEST_USER_EMAIL;
            active.password = BcryptUtil.bcryptHash(TEST_USER_PASSWORD, 12);
            active.role     = "user";
            active.active   = true;
            userLoginRepository.persist(active);

            UserLogin inactive = new UserLogin();
            inactive.email    = INACTIVE_EMAIL;
            inactive.password = BcryptUtil.bcryptHash(INACTIVE_PASSWORD, 12);
            inactive.role     = "user";
            inactive.active   = false;
            userLoginRepository.persist(inactive);
        });
    }

    @AfterEach
    void removeTestUsers() {
        QuarkusTransaction.requiringNew().run(() ->
                userLoginRepository.delete("email in (?1, ?2)", TEST_USER_EMAIL, INACTIVE_EMAIL));
    }

    private RequestSpecification asUser() {
        CookieFilter cookies = new CookieFilter();
        given()
                .filter(cookies)
                .redirects().follow(false)
                .formParam("j_username", TEST_USER_EMAIL)
                .formParam("j_password", TEST_USER_PASSWORD)
        .when()
                .post("/j_security_check")
        .then()
                .statusCode(302);
        return given().filter(cookies);
    }

    @Test
    void regularUser_forbiddenOnGenders() {
        asUser()
        .when()
                .get("/genders")
        .then()
                .statusCode(403);
    }

    @Test
    void regularUser_forbiddenOnTitles() {
        asUser()
        .when()
                .get("/titles")
        .then()
                .statusCode(403);
    }

    @Test
    void regularUser_forbiddenOnRelationships() {
        asUser()
        .when()
                .get("/relationships")
        .then()
                .statusCode(403);
    }

    @Test
    void inactiveUser_cannotAuthenticate() {
        given()
                .redirects().follow(false)
                .formParam("j_username", INACTIVE_EMAIL)
                .formParam("j_password", INACTIVE_PASSWORD)
        .when()
                .post("/j_security_check")
        .then()
                .statusCode(302)
                .header("Location", containsString("error"));
    }
}
